package com.umc.presentation.ui.notice.write

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.NoticeChipClassType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel @Inject constructor(
    // 더미 데이터를 사용하므로 UseCase 주입 없음
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    init {
        loadDummyUserInfoAndInit()
        loadDummyChapters()
        loadDummySchools()
        updatePartChipList(getPartChipList())
    }

    private fun loadDummyUserInfoAndInit() {
        val dummyUserInfo = UserInfo(
            id = 1L,
            name = "김철수",
            nickname = "철수왕",
            schoolId = 1L,
            schoolName = "홍익대학교",
            profileImageLink = "",
            status = "ACTIVE",
            roles = listOf(
                UserRole(
                    id = 1L,
                    challengerId = 1L,
                    roleType = "STAFF",
                    chapterId = 1L,
                    chapterName = "서울",
                    organizationType = "CENTRAL",
                    organizationId = null,
                    responsiblePart = "PM 파트",
                    gisuId = 9L,
                    gisu = 9L
                )
            )
        )
        updateState {
            copy(
                userInfo = dummyUserInfo,
                activeGisuId = 9
            )
        }
        // 더미: 모든 NoticeCategory를 드롭다운에 표시
        val allCategories = NoticeCategory.entries.map { it.label }
        updateDropDownList(allCategories)
        val defaultCategory = NoticeCategory.CENTRAL_OFFICE
        updateState { copy(category = defaultCategory) }
        updateClassChipList(getNoticeChip(defaultCategory))
    }

    private fun loadDummyChapters() {
        val dummyChapters = listOf(
            Chapter(id = 1, name = "서울"),
            Chapter(id = 2, name = "경기"),
            Chapter(id = 3, name = "부산"),
            Chapter(id = 4, name = "대구"),
            Chapter(id = 5, name = "광주")
        )
        updateState { copy(chapterList = dummyChapters) }
    }

    private fun loadDummySchools() {
        val dummySchools = listOf(
            SchoolInfo(schoolId = 1, schoolName = "홍익대학교"),
            SchoolInfo(schoolId = 2, schoolName = "서울대학교"),
            SchoolInfo(schoolId = 3, schoolName = "연세대학교"),
            SchoolInfo(schoolId = 4, schoolName = "고려대학교"),
            SchoolInfo(schoolId = 5, schoolName = "성균관대학교")
        )
        updateState { copy(schoolList = dummySchools) }
    }

    fun updateClassChipList(chipList: List<NoticeChipState>) {
        updateState { copy(classList = chipList) }
    }

    private fun updatePartChipList(chipList: List<NoticeChipState>) {
        updateState { copy(partList = chipList) }
    }

    private fun getNoticeChip(category: NoticeCategory): List<NoticeChipState> {
        val types = when (category) {
            NoticeCategory.CENTRAL_OFFICE -> {
                listOf(NoticeChipClassType.ALL, NoticeChipClassType.PART)
            }
            NoticeCategory.PART -> {
                listOf(NoticeChipClassType.PART)
            }
            NoticeCategory.SCHOOL -> {
                listOf(
                    NoticeChipClassType.ALL,
                    NoticeChipClassType.SCHOOL,
                    NoticeChipClassType.PART
                )
            }
            NoticeCategory.BRANCH -> {
                listOf(
                    NoticeChipClassType.ALL,
                    NoticeChipClassType.BRANCH,
                    NoticeChipClassType.PART
                )
            }
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
            if (it.text == chip.text) it.copy(isClicked = !it.isClicked) else it
        }

        updateState {
            copy(isShowPartChip = newList.any { it.text == NoticeChipClassType.PART.label && it.isClicked })
        }
        updateClassChipList(newList)
    }

    fun onChapterSelected(chapter: Chapter) {
        val currentList = uiState.value.classList.toMutableList()
        if (currentList.any { it.chapterId == chapter.id.toLong() }) return

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
        if (currentList.any { it.schoolId == school.schoolId.toLong() }) return

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
            if (it.text == chip.text) it.copy(isClicked = !it.isClicked) else it
        }
        updatePartChipList(newList)
    }

    fun updateDropDownList(list: List<String>) {
        updateState { copy(dropdownList = list) }
    }

    fun onClickShowDropDown() {
        if (!uiState.value.isShowCategoryDropDown) return
        updateState { copy(isShowDropDown = !uiState.value.isShowDropDown) }
    }

    fun updateCategory(category: NoticeCategory) {
        onClickShowDropDown()
        updateClassChipList(getNoticeChip(category))
        updateState { copy(category = category) }
    }

    fun onClickShowLink(flag: Boolean) {
        updateState { copy(isShowLink = flag) }
    }

    fun onClickShowVote() {
        emitEvent(NoticeWriteEvent.ShowBottomSheetEvent)
    }

    fun onClickCamera() {
        emitEvent(NoticeWriteEvent.SelectImageEvent)
    }

    fun updateSelectImage(list: List<Uri>) {
        updateState { copy(selectImageList = uiState.value.selectImageList + list) }
    }

    fun deleteImage(uri: Uri) {
        updateState { copy(selectImageList = uiState.value.selectImageList.filterNot { it == uri }) }
    }

    fun onChangedLinkText(text: String) {
        updateState { copy(linkText = text) }
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
            if (new.size > 2) new.removeAt(position) else new[position] = ""
            copy(voteTextList = new)
        }
    }

    fun onVoteAdd() {
        updateState {
            val new = uiState.value.voteTextList.toMutableList()
            new.add("")
            copy(voteTextList = new)
        }
    }

    fun onClickVoteAnonymity() {
        updateState { copy(canAnonymity = !canAnonymity) }
    }

    fun onClickVoteSelectMultiple() {
        updateState { copy(canSelectMultiple = !canSelectMultiple) }
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

    fun onClickToggleAllGisu() {
        updateState { copy(isAllGisuSelected = !isAllGisuSelected) }
    }

    // 서버 연결 없이 유효성 검사 후 바로 뒤로가기
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

        updateState { copy(isLoading = true) }
        delay(500) // 가짜 제출 딜레이

        updateState { copy(isLoading = false) }
        emitEvent(NoticeWriteEvent.SubmitSuccess)
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
    val category: NoticeCategory = NoticeCategory.CENTRAL_OFFICE,
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
    val userInfo: UserInfo? = null,
    val isAllGisuSelected: Boolean = false,
) : UiState {
    val isShowClassSection: Boolean
        get() = category != NoticeCategory.PART

    val isShowCategoryDropDown: Boolean
        get() = dropdownList.size > 1
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

