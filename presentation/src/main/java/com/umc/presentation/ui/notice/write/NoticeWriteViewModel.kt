package com.umc.presentation.ui.notice.write

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeTargetRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.usecase.notice.AddNoticeImagesUseCase
import com.umc.domain.usecase.notice.AddNoticeLinksUseCase
import com.umc.domain.usecase.notice.AddNoticeVoteUseCase
import com.umc.domain.usecase.notice.CreateNoticeUseCase
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel
@Inject
constructor(
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val addNoticeVoteUseCase: AddNoticeVoteUseCase,
    private val addNoticeImagesUseCase: AddNoticeImagesUseCase,
    private val addNoticeLinksUseCase: AddNoticeLinksUseCase,
    private val uploadFileUseCase: UploadFileUseCase
) : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    init {
        updateDropDownList(dropDown())
        updateClassChipList(getDummy())
        updatePartChipList(getDummy2())
    }

    private fun updateClassChipList(chipList: List<NoticeChipState>) {
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

    private fun getDummy(): List<NoticeChipState> {
        return listOf(
            NoticeChipState(
                text = "전체",
                isClicked = true,
            ),
            NoticeChipState(
                text = "운영진공지",
                isClicked = false,
            ),
            NoticeChipState(
                text = "파트",
                isClicked = false,
            ),
            NoticeChipState(
                text = "지부",
                isClicked = false,
                hanBottomSheet = true
            ),
            NoticeChipState(
                text = "학교",
                isClicked = false,
                hanBottomSheet = true
            )
        )
    }

    private fun getDummy2(): List<NoticeChipState> {
        return listOf(
            NoticeChipState(
                text = "Web",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Server",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Ios",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Android",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Design",
                isClicked = false,
            ),
            NoticeChipState(
                text = "PM",
                isClicked = false,
            ),
            NoticeChipState(
                text = "SpringBoot",
                isClicked = false,
            ),
            NoticeChipState(
                text = "Node.js",
                isClicked = false,
            )
        )
    }

    fun onClickClassChip(chip: NoticeChipState) {
        val newList = uiState.value.classList.map {
            if (it.text == chip.text) {
                it.copy(isClicked = !it.isClicked)
            } else {
                it
            }
        }

        //TODO 임시임 게시판 분류 확인되면 수정할 예정
        if (newList.any { it.text == "파트" && it.isClicked }) {
            updateState { copy(isShowPartChip = true) }
        } else {
            updateState { copy(isShowPartChip = false) }
        }

        updateClassChipList(newList)
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
        updateState {
            copy(category = category)
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

    fun onChangedLinkText(text: String){
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

    fun onClickVoteAnonymity(){
        updateState {
            copy(canAnonymity = !canAnonymity)
        }
    }

    fun onClickVoteSelectMultiple(){
        updateState {
            copy(canSelectMultiple = !canSelectMultiple)
        }
    }

    fun updateVoteTitle(title: String) {
        updateState { copy(voteTitle = title) }
    }

    // Title and Content
    fun updateTitle(title: String) {
        updateState { copy(title = title) }
    }

    fun updateContent(content: String) {
        updateState { copy(content = content) }
    }

    fun updateShouldNotify(shouldNotify: Boolean) {
        updateState { copy(shouldNotify = shouldNotify) }
    }

    // Submit Notice
    fun onClickSubmit() = viewModelScope.launch {
        val state = uiState.value

        // Validate inputs
        if (state.title.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError("제목을 입력해주세요"))
            return@launch
        }
        if (state.content.isBlank()) {
            emitEvent(NoticeWriteEvent.ShowError("내용을 입력해주세요"))
            return@launch
        }

        updateState { copy(isLoading = true) }

        val imageIds = uploadImages(state.selectImageList)

        val targetParts = state.partList.filter { it.isClicked }.map { it.text }
        val targetRequest = NoticeTargetRequest(
            targetGisuId = 1, // TODO: Get from selected gisu
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
                // Notice created successfully, now add optional components
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
                    startsAt = "", // TODO: Add vote start time if needed
                    endsAtExclusive = "", // TODO: Add vote end time if needed
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
    val canAnonymity: Boolean = false,
    val canSelectMultiple: Boolean = false,
    val title: String = "",
    val content: String = "",
    val shouldNotify: Boolean = true,
    val isLoading: Boolean = false
) : UiState

sealed interface NoticeWriteEvent : UiEvent {
    object SelectImageEvent : NoticeWriteEvent
    object ShowBottomSheetEvent: NoticeWriteEvent
    object SubmitSuccess : NoticeWriteEvent
    data class ShowError(val message: String) : NoticeWriteEvent
}
