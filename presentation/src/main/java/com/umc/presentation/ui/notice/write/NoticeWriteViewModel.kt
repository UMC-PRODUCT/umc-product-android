package com.umc.presentation.ui.notice.write

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.NoticeChipClassType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.UserInfo
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeTargetRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.notice.AddNoticeImagesUseCase
import com.umc.domain.usecase.notice.AddNoticeLinksUseCase
import com.umc.domain.usecase.notice.AddNoticeVoteUseCase
import com.umc.domain.usecase.notice.CreateNoticeUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.UpdateNoticeUseCase
import com.umc.domain.usecase.organization.GetChapterListUseCase
import com.umc.domain.usecase.organization.GetChapterWithSchoolUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.notice.write.model.NoticeImageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val updateNoticeUseCase: UpdateNoticeUseCase,
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val addNoticeVoteUseCase: AddNoticeVoteUseCase,
    private val addNoticeImagesUseCase: AddNoticeImagesUseCase,
    private val addNoticeLinksUseCase: AddNoticeLinksUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
    private val getChapterWithSchoolUseCase: GetChapterWithSchoolUseCase,
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    fun initEditMode(noticeId: Long) = viewModelScope.launch {
        if (noticeId == 0L) {
            emitEvent(NoticeWriteEvent.ShowError("유효하지 않은 공지사항 ID입니다"))
            return@launch
        }

        updateState { copy(isLoading = true, isEditMode = true, editNoticeId = noticeId) }

        // 상세 조회 API로 기존 데이터 가져오기
        resultResponse(
            response = getNoticeDetailUseCase(noticeId),
            successCallback = { detail ->
                val existingImages = detail.images.map { image ->
                    NoticeImageItem(
                        id = image.id,
                        url = image.url
                    )
                }
                val linkText = detail.links.joinToString(", ") { it.url }
                
                updateState {
                    copy(
                        isLoading = false,
                        title = detail.title,
                        content = detail.content,
                        selectImageList = existingImages,
                        isShowLink = detail.links.isNotEmpty(),
                        linkText = linkText
                    )
                }
            },
            errorCallback = {
                updateState { copy(isLoading = false) }
                emitEvent(NoticeWriteEvent.ShowError("공지사항 정보를 불러오는데 실패했습니다"))
            }
        )
    }

    init {
        loadUserInfoAndInit()
        loadChapters()
        loadSchools()
        updatePartChipList(getPartChipList())
    }

    fun setSelectedGisu(gisuId: Long, gisuName: String) {
        if (gisuId > 0) {
            updateState {
                copy(
                    activeGisuId = gisuId.toInt(),
                    activeGisuName = gisuName.takeIf { it.isNotEmpty() }
                )
            }
            // 특정 기수가 선택되고, 전체 기수가 아닌 경우 해당 기수의 지부/학교 로드
            if (!uiState.value.isAllGisuSelected) {
                loadChaptersAndSchoolsForGisu(gisuId.toInt())
            }
        }
    }

    private fun loadUserInfoAndInit() = viewModelScope.launch {
        getUserInfoUseCase().collect { userInfo ->
            updateState {
                copy(
                    userInfo = userInfo,
                )
            }
            updateClassChipList(getNoticeChip())
        }
    }

    private fun loadChapters() = viewModelScope.launch {
        resultResponse(
            response = getChapterListUseCase(),
            successCallback = { chapters ->
                updateState { copy(chapterList = chapters) }
            }
        )
    }

    private fun loadSchools() = viewModelScope.launch {
        resultResponse(
            response = getAllSchoolUseCase(),
            successCallback = { schools ->
                updateState { copy(schoolList = schools) }
            }
        )
    }

    /**
     * 특정 기수에 해당하는 지부와 학교 리스트를 로드
     * 전체 기수가 아닌 경우에 사용
     */
    private fun loadChaptersAndSchoolsForGisu(gisuId: Int) = viewModelScope.launch {
        resultResponse(
            response = getChapterWithSchoolUseCase(gisuId),
            successCallback = { chapterWithSchool ->
                updateState {
                    copy(
                        chapterList = chapterWithSchool.chapterList,
                        schoolList = chapterWithSchool.schoolList
                    )
                }
            }
        )
    }

    fun updateClassChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                classList = chipList
            )
        }
    }

    private fun updatePartChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                partList = chipList
            )
        }
    }

    private fun getNoticeChip(): List<NoticeChipState> {
        val types = listOf(
            NoticeChipClassType.BRANCH,
            NoticeChipClassType.SCHOOL,
            NoticeChipClassType.PART
        )
        
        return types.map { type ->
            NoticeChipState(
                text = type.label,
                isClicked = type == NoticeChipClassType.ALL,
                hanBottomSheet = type.hasBottomSheet
            )
        }
    }

    private fun getPartChipList(): List<NoticeChipState> {
        return UserPart.entries
            .filter { it != UserPart.UNKNOWN }
            .map { part ->
                NoticeChipState(
                    text = part.label,
                    isClicked = false,
                    part = part.name
                )
            }
    }

    fun onClickClassChip(chip: NoticeChipState) {
        val selectedType = NoticeChipClassType.fromLabel(chip.text)

        if (selectedType.hasBottomSheet) {
            // BRANCH 또는 SCHOOL을 선택한 경우에는 항상 BottomSheet만 표시
            // 선택/해제는 BottomSheet에서 하위 항목을 선택/재선택하여 처리
            when (selectedType) {
                NoticeChipClassType.BRANCH -> emitEvent(NoticeWriteEvent.ShowChapterBottomSheetEvent)
                NoticeChipClassType.SCHOOL -> emitEvent(NoticeWriteEvent.ShowSchoolBottomSheetEvent)
                else -> {}
            }
            return
        }

        // PART 클릭 처리
        val newList = uiState.value.classList.map {
            if (it.text == chip.text) {
                it.copy(isClicked = !it.isClicked)
            } else {
                it
            }
        }

        if (newList.any { it.text == NoticeChipClassType.PART.label && it.isClicked }) {
            updateState { copy(isShowPartChip = true) }
        } else {
            updateState { copy(isShowPartChip = false) }
        }

        updateClassChipList(newList)
    }

    private fun unselectBranchIfSelected(currentList: MutableList<NoticeChipState>): MutableList<NoticeChipState> {
        val branchIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.BRANCH.label }
        if (branchIndex != -1) {
            currentList[branchIndex] = currentList[branchIndex].copy(
                isClicked = false,
                chapterId = null,
                selectedDisplayName = null
            )
        }
        return currentList
    }

    private fun unselectSchoolIfSelected(currentList: MutableList<NoticeChipState>): MutableList<NoticeChipState> {
        val schoolIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.SCHOOL.label }
        if (schoolIndex != -1) {
            currentList[schoolIndex] = currentList[schoolIndex].copy(
                isClicked = false,
                schoolId = null,
                selectedDisplayName = null
            )
        }
        return currentList
    }

    fun onChapterSelected(chapter: Chapter) {
        var currentList = uiState.value.classList.toMutableList()
        val branchIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.BRANCH.label }

        // 이미 선택된 chapter인지 확인
        val alreadySelectedChapter = currentList.find { it.chapterId == chapter.id.toLong() }

        if (alreadySelectedChapter != null) {
            // 이미 선택된 경우 선택 해제 (chapterId 제거, isClicked = false, selectedDisplayName = null)
            if (branchIndex != -1) {
                currentList[branchIndex] = currentList[branchIndex].copy(
                    isClicked = false,
                    chapterId = null,
                    selectedDisplayName = null
                )
            }
            updateClassChipList(currentList)
            return
        }

        // 학교 선택 해제 (지부 선택 시 학교는 해제되어야 함)
        currentList = unselectSchoolIfSelected(currentList)

        // BRANCH 칩을 활성화하고 선택된 chapter 정보 저장
        if (branchIndex != -1) {
            currentList[branchIndex] = currentList[branchIndex].copy(
                isClicked = true,
                chapterId = chapter.id.toLong(),
                selectedDisplayName = chapter.name
            )
        }

        updateClassChipList(currentList)
    }

    fun onSchoolSelected(school: SchoolInfo) {
        var currentList = uiState.value.classList.toMutableList()
        val schoolIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.SCHOOL.label }

        // 이미 선택된 school인지 확인
        val alreadySelectedSchool = currentList.find { it.schoolId == school.schoolId.toLong() }

        if (alreadySelectedSchool != null) {
            // 이미 선택된 경우 선택 해제 (schoolId 제거, isClicked = false, selectedDisplayName = null)
            if (schoolIndex != -1) {
                currentList[schoolIndex] = currentList[schoolIndex].copy(
                    isClicked = false,
                    schoolId = null,
                    selectedDisplayName = null
                )
            }
            updateClassChipList(currentList)
            return
        }

        // 지부 선택 해제 (학교 선택 시 지부는 해제되어야 함)
        currentList = unselectBranchIfSelected(currentList)

        // SCHOOL 칩을 활성화하고 선택된 school 정보 저장
        if (schoolIndex != -1) {
            currentList[schoolIndex] = currentList[schoolIndex].copy(
                isClicked = true,
                schoolId = school.schoolId.toLong(),
                selectedDisplayName = school.schoolName
            )
        }

        updateClassChipList(currentList)
    }

    fun onClickPartChip(chip: NoticeChipState) {
        val newList = uiState.value.partList.map {
            if (it.text == chip.text) {
                it.copy(isClicked = !it.isClicked)
            } else {
                it
            }
        }

        updatePartChipList(newList)
    }

    fun onClickShowLink(flag: Boolean) {
        updateState {
            copy(isShowLink = flag)
        }
    }

    fun onClickShowVote() {
        emitEvent(NoticeWriteEvent.ShowBottomSheetEvent)
    }

    fun onClickCamera() {
        emitEvent(NoticeWriteEvent.SelectImageEvent)
    }

    fun updateSelectImage(list: List<Uri>) {
        val newImages = list.map { uri ->
            NoticeImageItem(uri = uri)
        }
        updateState {
            copy(selectImageList = uiState.value.selectImageList + newImages)
        }
    }

    fun deleteImage(item: NoticeImageItem) {
        updateState {
            copy(
                selectImageList = uiState.value.selectImageList.filterNot {
                    if (it.id != 0L && item.id != 0L) {
                        it.id == item.id
                    } else {
                        it.uri == item.uri
                    }
                }
            )
        }
    }

    fun onChangedLinkText(text: String) {
        updateState {
            copy(linkText = text)
        }
    }

    fun updateVoteList(list: List<String>) {
        val anonymity = if (uiState.value.canAnonymity) "익명, " else "실명, "
        val multiple = if (uiState.value.canSelectMultiple) "복수 허용, " else "단일 투표, "
        val count = "${list.size}개의 항목"
        updateState {
            copy(
                isShowVote = true,
                voteTextList = list,
                voteCondition = anonymity + multiple + count
            )
        }
    }

    fun onVoteDelete(position: Int) {
        updateState {
            val cur = uiState.value.voteTextList
            val new = cur.toMutableList()
            if (new.size > 2) {
                new.removeAt(position)
            } else {
                new[position] = ""
            }

            copy(voteTextList = new)
        }
    }

    fun onVoteAdd() {
        updateState {
            val cur = uiState.value.voteTextList
            val new = cur.toMutableList()
            new.add("")
            copy(voteTextList = new)
        }
    }

    fun onClickVoteAnonymity() {
        updateState {
            copy(canAnonymity = !canAnonymity)
        }
    }

    fun onClickVoteSelectMultiple() {
        updateState {
            copy(canSelectMultiple = !canSelectMultiple)
        }
    }

    fun updateVoteTitle(title: String) {
        updateState { copy(voteTitle = title) }
    }

    fun updateVoteStartDate(date: String) {
        updateState { copy(voteStartDate = date) }
    }

    fun updateVoteEndDate(date: String) {
        updateState { copy(voteEndDate = date) }
    }

    /**
     * Reset vote settings (local only, no server call)
     */
    fun deleteVote(onSuccess: () -> Unit = {}) {
        updateState {
            copy(
                isShowVote = false,
                voteTitle = "투표 만들기",
                voteTextList = List(2) { "" },
                canAnonymity = false,
                canSelectMultiple = false,
                voteStartDate = formatDateForDisplay(Calendar.getInstance()),
                voteEndDate = formatDateForDisplay(Calendar.getInstance()),
            )
        }
        onSuccess()
    }

    /**
     * Update vote settings from current form values (local only, no server call)
     */
    fun updateVote(list: List<String>, onSuccess: () -> Unit = {}) {
        val anonymity = if (uiState.value.canAnonymity) "익명, " else "실명, "
        val multiple = if (uiState.value.canSelectMultiple) "복수 허용, " else "단일 투표, "
        val count = "${list.size}개의 항목"
        updateState {
            copy(
                isShowVote = true,
                voteTextList = list,
                voteCondition = anonymity + multiple + count
            )
        }
        onSuccess()
    }

    fun updateTitle(title: String) {
        updateState { copy(title = title) }
    }

    fun updateContent(content: String) {
        updateState { copy(content = content) }
    }

    fun updateShouldNotify(shouldNotify: Boolean) {
        updateState { copy(shouldNotify = shouldNotify) }
    }

    fun onClickToggleAllGisu() {
        val newIsAllGisuSelected = !uiState.value.isAllGisuSelected
        updateState { copy(isAllGisuSelected = newIsAllGisuSelected) }
        
        // 전체 기수 토글 시 지부/학교 리스트 갱신
        if (newIsAllGisuSelected) {
            // 전체 기수 선택됨 -> 전체 지부/학교 로드
            loadChapters()
            loadSchools()
        } else {
            // 특정 기수 선택됨 -> 해당 기수의 지부/학교 로드
            uiState.value.activeGisuId?.let { gisuId ->
                loadChaptersAndSchoolsForGisu(gisuId)
            }
        }
    }

    fun onClickSubmit() = viewModelScope.launch {
        val state = uiState.value

        if (state.title.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError("제목을 입력해주세요"))
            return@launch
        }
        if (state.content.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError("내용을 입력해주세요"))
            return@launch
        }

        val activeGisuId = state.activeGisuId
        if (activeGisuId == null) {
            emitEvent(NoticeWriteEvent.ShowError("기수 정보를 불러오는 중입니다"))
            return@launch
        }

        updateState { copy(isLoading = true) }

        if (state.isEditMode) {
            // 수정 모드
            handleNoticeUpdate()
        } else {
            // 생성 모드
            val targetParts = state.partList.filter { it.isClicked }.map { it.text.uppercase() }
            val targetChapterId = state.classList.find { it.isClicked && it.chapterId != null }?.chapterId?.toInt()
            val targetSchoolId = state.classList.find { it.isClicked && it.schoolId != null }?.schoolId?.toInt()
            val targetRequest = NoticeTargetRequest(
                targetGisuId = if (state.isAllGisuSelected) null else activeGisuId,
                targetChapterId = targetChapterId,
                targetSchoolId = targetSchoolId,
                targetParts = targetParts
            )

            val createRequest = NoticeCreateRequest(
                title = state.title,
                content = state.content,
                shouldNotify = state.shouldNotify,
                targetInfo = targetRequest
            )

            resultResponse(
                response = createNoticeUseCase(createRequest),
                successCallback = { noticeId ->
                    handleNoticeCreated(noticeId)
                },
                errorCallback = {
                    updateState { copy(isLoading = false) }
                    emitEvent(NoticeWriteEvent.ShowError(it.message))
                }
            )
        }
    }

    private fun handleNoticeUpdate() = viewModelScope.launch {
        val state = uiState.value
        var hasError = false

        val updateRequest = NoticeUpdateRequest(
            title = state.title,
            content = state.content
        )
        resultResponse(
            response = updateNoticeUseCase(state.editNoticeId, updateRequest),
            successCallback = { },
            errorCallback = { hasError = true }
        )
        
        val existingImageIds = state.selectImageList
            .filter { it.isExistingImage() }
            .map { it.id.toString() }
        
        val newImages = state.selectImageList.filter { it.isNewImage() }
        val uploadedImageIds = if (newImages.isNotEmpty()) {
            val newImageUris = newImages.mapNotNull { it.uri }
            uploadImages(newImageUris)
        } else {
            emptyList()
        }
        
        val allImageIds = existingImageIds + uploadedImageIds
        
        if (allImageIds.isNotEmpty()) {
            resultResponse(
                response = addNoticeImagesUseCase(state.editNoticeId, allImageIds),
                successCallback = { },
                errorCallback = { hasError = true }
            )
        }

        if (state.isShowLink && state.linkText.isNotBlank()) {
            val links = state.linkText.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (links.isNotEmpty()) {
                resultResponse(
                    response = addNoticeLinksUseCase(state.editNoticeId, links),
                    successCallback = { },
                    errorCallback = { hasError = true }
                )
            }
        }

        updateState { copy(isLoading = false) }

        if (!hasError) {
            emitEvent(NoticeWriteEvent.SubmitSuccess)
        } else {
            emitEvent(NoticeWriteEvent.ShowError("일부 항목 수정에 실패했습니다"))
        }
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val imageIds = mutableListOf<String>()
        imageUris.forEachIndexed { index, uri ->
            resultResponse(
                response = uploadFileUseCase(uri.toString(), UploadFileCategory.NOTICE_ATTACHMENT),
                successCallback = {
                    imageIds.add(it.fileId)
                }
            )
        }
        return imageIds
    }

    private fun handleNoticeCreated(
        noticeId: Long
    ) = viewModelScope.launch {
        var hasError = false
        val state = uiState.value

        if (state.selectImageList.isNotEmpty()) {
            val newImageUris = state.selectImageList.mapNotNull { it.uri }
            val imageIds = uploadImages(newImageUris)
            if (imageIds.isNotEmpty()) {
                resultResponse(
                    response = addNoticeImagesUseCase(noticeId, imageIds),
                    successCallback = { },
                    errorCallback = { hasError = true }
                )
            }
        }

        if (state.isShowLink && state.linkText.isNotBlank()) {
            val links = state.linkText.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (links.isNotEmpty()) {
                resultResponse(
                    response = addNoticeLinksUseCase(noticeId, links),
                    successCallback = { },
                    errorCallback = { hasError = true }
                )
            }
        }

        if (state.isShowVote && state.voteTextList.isNotEmpty()) {
                            val validOptions = state.voteTextList.filter { it.isNotBlank() }
                if (validOptions.isNotEmpty()) {
                    val voteRequest = NoticeVoteRequest(
                        title = state.voteTitle,
                        isAnonymous = state.canAnonymity,
                        allowMultipleChoice = state.canSelectMultiple,
                        startsAt = formatDateForServer(state.voteStartDate),
                        endsAtExclusive = formatDateForServer(state.voteEndDate),
                        options = validOptions
                )
                resultResponse(
                    response = addNoticeVoteUseCase(noticeId, voteRequest),
                    successCallback = { },
                    errorCallback = { hasError = true }
                )
            }
        }

        updateState { copy(isLoading = false) }

        if (!hasError) {
            emitEvent(NoticeWriteEvent.SubmitSuccess)
        } else {
            emitEvent(NoticeWriteEvent.ShowError("일부 항목 추가에 실패했습니다"))
        }
    }
}

data class NoticeWriteUiState(
    val isShowPartChip: Boolean = false,
    val isShowLink: Boolean = false,
    val isShowVote: Boolean = false,
    val selectImageList: List<NoticeImageItem> = emptyList(), // 통합 이미지 리스트 (id, uri, url)
    val classList: List<NoticeChipState> = emptyList(),
    val partList: List<NoticeChipState> = emptyList(),
    val linkText: String = "",
    val voteTitle: String = "투표 만들기",
    val voteCondition: String = "",
    val voteTextList: List<String> = List(2) { "" },
    val voteStartDate: String = formatDateForDisplay(Calendar.getInstance()),
    val voteEndDate: String = formatDateForDisplay(Calendar.getInstance()),
    val canAnonymity: Boolean = false,
    val canSelectMultiple: Boolean = false,
    val title: String = "",
    val content: String = "",
    val shouldNotify: Boolean = true,
    val isLoading: Boolean = false,
    val chapterList: List<Chapter> = emptyList(),
    val schoolList: List<SchoolInfo> = emptyList(),
    val activeGisuId: Int? = null,
    val activeGisuName: String? = null,
    val userInfo: UserInfo? = null,
    val isAllGisuSelected: Boolean = false,
    val isEditMode: Boolean = false,
    val editNoticeId: Long = 0L,
) : UiState

sealed interface NoticeWriteEvent : UiEvent {
    object SelectImageEvent : NoticeWriteEvent
    object ShowBottomSheetEvent : NoticeWriteEvent
    object ShowChapterBottomSheetEvent : NoticeWriteEvent
    object ShowSchoolBottomSheetEvent : NoticeWriteEvent
    object SubmitSuccess : NoticeWriteEvent
    data class ShowError(val message: String) : NoticeWriteEvent
}

// 날짜를 "YYYY년 MM월 DD일" 형식으로 변환
fun formatDateForDisplay(calendar: Calendar): String {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return "${year}년 ${month}월 ${day}일"
}

// 날짜를 ISO 8601 형식(2026-02-15T18:35:12.445Z)으로 변환
fun formatDateForServer(dateString: String): String {
    // "2025년 2월 16일" -> ISO 8601
    val regex = """(\d{4})년 (\d{1,2})월 (\d{1,2})일""".toRegex()
    val matchResult = regex.find(dateString)
    
    return if (matchResult != null) {
        val (year, month, day) = matchResult.destructured
        // ISO 8601 형식: YYYY-MM-DDTHH:mm:ss.sssZ
        String.format(Locale.US, "%04d-%02d-%02dT00:00:00.000Z", year.toInt(), month.toInt(), day.toInt())
    } else {
        // 파싱 실패 시 현재 시간 반환
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date())
    }
}
