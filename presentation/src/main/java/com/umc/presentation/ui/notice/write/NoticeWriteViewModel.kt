package com.umc.presentation.ui.notice.write

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.NoticeChipClassType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeTargetRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.usecase.notice.AddNoticeImagesUseCase
import com.umc.domain.usecase.notice.AddNoticeLinksUseCase
import com.umc.domain.usecase.notice.AddNoticeVoteUseCase
import com.umc.domain.usecase.notice.CreateNoticeUseCase
import com.umc.domain.usecase.organization.GetChapterListUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
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
    private val addNoticeVoteUseCase: AddNoticeVoteUseCase,
    private val addNoticeImagesUseCase: AddNoticeImagesUseCase,
    private val addNoticeLinksUseCase: AddNoticeLinksUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val getGisuListUseCase: GetGisuListUseCase
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    init {
        updateDropDownList(dropDown())
        updateClassChipList(getNoticeChip(NoticeCategory.SCHOOL))
        updatePartChipList(getPartChipList())
        loadChapters()
        loadSchools()
        loadGisuList()
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

    private fun loadGisuList() = viewModelScope.launch {
        resultResponse(
            response = getGisuListUseCase(),
            successCallback = { gisuList ->
                val activeGisu = gisuList.gisuList.find { it.isActive }
                updateState { copy(activeGisuId = activeGisu?.gisuId) }
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

    private fun getNoticeChip(category: NoticeCategory): List<NoticeChipState> {
        val types = when (category) {
            NoticeCategory.SCHOOL -> listOf(
                NoticeChipClassType.ALL,
                NoticeChipClassType.OPERATOR,
                NoticeChipClassType.PART
            )
            NoticeCategory.CENTRAL_OFFICE -> listOf(
                NoticeChipClassType.ALL,
                NoticeChipClassType.OPERATOR,
                NoticeChipClassType.PART,
                NoticeChipClassType.BRANCH
            )
            NoticeCategory.BRANCH -> listOf(
                NoticeChipClassType.ALL,
                NoticeChipClassType.OPERATOR,
                NoticeChipClassType.PART,
                NoticeChipClassType.SCHOOL
            )
            NoticeCategory.PART -> emptyList()
        }
        
        return types.map { type ->
            NoticeChipState(
                text = type.label,
                isClicked = type == NoticeChipClassType.ALL,
                hanBottomSheet = type.hasBottomSheet
            )
        }
    }

    private fun getPartChipList(): List<NoticeChipState> {
        return UserPart.entries.map { part ->
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
            when (selectedType) {
                NoticeChipClassType.BRANCH -> emitEvent(NoticeWriteEvent.ShowChapterBottomSheetEvent)
                NoticeChipClassType.SCHOOL -> emitEvent(NoticeWriteEvent.ShowSchoolBottomSheetEvent)
                else -> {}
            }
            return
        }

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

    fun onChapterSelected(chapter: Chapter) {
        val currentList = uiState.value.classList.toMutableList()

        val alreadySelected = currentList.any { it.chapterId == chapter.id.toLong() }
        if (alreadySelected) return

        val selectedChapterChip = NoticeChipState(
            text = chapter.name,
            isClicked = true,
            chapterId = chapter.id.toLong(),
            hanBottomSheet = false
        )

        val branchChipIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.BRANCH.label }
        if (branchChipIndex != -1) {
            currentList.add(branchChipIndex, selectedChapterChip)
        } else {
            currentList.add(selectedChapterChip)
        }

        updateClassChipList(currentList)
    }

    fun onSchoolSelected(school: SchoolInfo) {
        val currentList = uiState.value.classList.toMutableList()

        val alreadySelected = currentList.any { it.schoolId == school.schoolId.toLong() }
        if (alreadySelected) return

        val selectedSchoolChip = NoticeChipState(
            text = school.schoolName,
            isClicked = true,
            schoolId = school.schoolId.toLong(),
            hanBottomSheet = false
        )

        val schoolChipIndex = currentList.indexOfFirst { it.text == NoticeChipClassType.SCHOOL.label }
        if (schoolChipIndex != -1) {
            currentList.add(schoolChipIndex, selectedSchoolChip)
        } else {
            currentList.add(selectedSchoolChip)
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

    fun updateDropDownList(list: List<String>) {
        updateState {
            copy(dropdownList = list)
        }
    }

    private fun dropDown(): List<String> {
        return listOf(
            NoticeCategory.SCHOOL.label,
            NoticeCategory.CENTRAL_OFFICE.label,
            NoticeCategory.BRANCH.label,
            NoticeCategory.PART.label
        )
    }

    fun onClickShowDropDown() {
        updateState {
            copy(isShowDropDown = !uiState.value.isShowDropDown)
        }
    }

    fun updateCategory(category: NoticeCategory) {
        onClickShowDropDown()
        updateClassChipList(getNoticeChip(category))
        updateState {
            copy(
                category = category,
            )
        }
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
        updateState {
            copy(selectImageList = uiState.value.selectImageList + list)
        }
    }

    fun deleteImage(uri: Uri) {
        updateState {
            copy(
                selectImageList = uiState.value.selectImageList.filterNot { it == uri }
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

    fun updateTitle(title: String) {
        updateState { copy(title = title) }
    }

    fun updateContent(content: String) {
        updateState { copy(content = content) }
    }

    fun updateShouldNotify(shouldNotify: Boolean) {
        updateState { copy(shouldNotify = shouldNotify) }
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

        val imageIds = uploadImages(state.selectImageList)

        val targetParts = state.partList.filter { it.isClicked }.map { it.text }
        val targetRequest = NoticeTargetRequest(
            targetGisuId = activeGisuId,
            targetChapterId = null,
            targetSchoolId = null,
            targetParts = targetParts
        )

        // 3. Create Notice
        val createRequest = NoticeCreateRequest(
            title = state.title,
            content = state.content,
            shouldNotify = state.shouldNotify,
            targetInfo = targetRequest
        )

        resultResponse(
            response = createNoticeUseCase(createRequest),
            successCallback = { noticeId ->
                handleNoticeCreated(noticeId, imageIds, state)
            },
            errorCallback = {
                updateState { copy(isLoading = false) }
                emitEvent(NoticeWriteEvent.ShowError("공지사항 작성에 실패했습니다"))
            }
        )
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val imageIds = mutableListOf<String>()
        imageUris.forEach { uri ->
            resultResponse(
                response = uploadFileUseCase(uri.toString(), UploadFileCategory.NOTICE_ATTACHMENT),
                successCallback = { uploadInfo ->
                    imageIds.add(uploadInfo.fileId)
                }
            )
        }
        return imageIds
    }

    private fun handleNoticeCreated(
        noticeId: Long,
        imageIds: List<String>,
        state: NoticeWriteUiState
    ) = viewModelScope.launch {
        var hasError = false

        // Add images if any
        if (imageIds.isNotEmpty()) {
            resultResponse(
                response = addNoticeImagesUseCase(noticeId, imageIds),
                successCallback = { },
                errorCallback = { hasError = true }
            )
        }

        // Add links if any
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
    val isShowDropDown: Boolean = false,
    val isShowPartChip: Boolean = false,
    val isShowLink: Boolean = false,
    val isShowVote: Boolean = false,
    val selectImageList: List<Uri> = emptyList(),
    val classList: List<NoticeChipState> = emptyList(),
    val partList: List<NoticeChipState> = emptyList(),
    val category: NoticeCategory = NoticeCategory.SCHOOL,
    val dropdownList: List<String> = emptyList(),
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
) : UiState {
    val isShowClassSection: Boolean
        get() = category != NoticeCategory.PART
}

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
