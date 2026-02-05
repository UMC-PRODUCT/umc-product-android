package com.umc.presentation.ui.notice.write

import android.net.Uri
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeWriteViewModel
@Inject
constructor() : BaseViewModel<NoticeWriteUiState, NoticeWriteEvent>(
    NoticeWriteUiState(),
) {
    init {
        updateDropDownList(dummyDropDown())
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

    private fun dummyDropDown(): List<String> {
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
    val voteTitle: String = "",
    val voteCondition: String = "",
    val voteTextList: List<String> = List(2) { "" },
    val canAnonymity: Boolean = false,
    val canSelectMultiple: Boolean = false,
) : UiState

sealed interface NoticeWriteEvent : UiEvent {
    object SelectImageEvent : NoticeWriteEvent
    object ShowBottomSheetEvent: NoticeWriteEvent

}
