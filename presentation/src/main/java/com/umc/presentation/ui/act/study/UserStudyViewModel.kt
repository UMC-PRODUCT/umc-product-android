package com.umc.presentation.ui.act.study

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState

data class UserStudyState(
    val title: String = "웹 프론트엔드 기초",
    val items: List<ActStudyItemUiModel> = emptyList(),
) : UiState {

    val totalCount: Int get() = items.size
    val passCount: Int get() = items.count { it.status == StudyStatus.PASS }

    val progress: Int
        get() = if (totalCount == 0) 0 else (passCount * 100 / totalCount)

    val percentText: String get() = "$progress%"
    val subText: String get() = "${passCount}/${totalCount} 완료"
}

sealed interface UserStudyEvent : UiEvent

class UserStudyViewModel : BaseViewModel<UserStudyState, UserStudyEvent>(
    UserStudyState(
        items = listOf(
            ActStudyItemUiModel(1, "Github", "HTML/CSS 기초", StudyStatus.PASS),
            ActStudyItemUiModel(2, "Github", "HTML/CSS 기초", StudyStatus.PASS),
            ActStudyItemUiModel(3, "Github", "HTML/CSS 기초", StudyStatus.PASS),
            ActStudyItemUiModel(4, "Github", "HTML/CSS 기초", StudyStatus.IN_PROGRESS),
            ActStudyItemUiModel(5, "Github", "HTML/CSS 기초", StudyStatus.IN_PROGRESS),
            ActStudyItemUiModel(6, "Github", "HTML/CSS 기초", StudyStatus.IN_PROGRESS),
        )
    )

) {

    init {
        updateState { copy(items = applyLockRule(items)) }
    }

    fun toggleExpand(index: Int) {
        updateState {
            val target = items.getOrNull(index) ?: return@updateState this

            if (target.isLocked) return@updateState this

            val updated = items.mapIndexed { i, item ->
                if (i == index) item.copy(isExpanded = !item.isExpanded) else item
            }

            copy(items = applyLockRule(updated))
        }
    }

    fun onSubmitClick(itemId: Long, link: String) {
        updateItem(itemId) { item ->
            when (item.submitState) {
                SubmitState.REQUESTED -> item

                SubmitState.CONFIRMING -> item.copy(
                    submitState = SubmitState.REQUESTED
                )

                SubmitState.IDLE,
                SubmitState.READY -> {
                    if (link.isBlank()) item
                    else item.copy(
                        link = link,
                        submitState = SubmitState.CONFIRMING
                    )
                }
            }
        }
    }

    fun debugApprove(itemId: Long) {
        updateItem(itemId) { it.copy(status = StudyStatus.PASS) }
    }

    private fun updateItem(
        itemId: Long,
        transform: (ActStudyItemUiModel) -> ActStudyItemUiModel
    ) {
        updateState {
            val updated = items.map { item ->
                if (item.id == itemId) transform(item) else item
            }
            copy(items = applyLockRule(updated))
        }
    }


    private fun applyLockRule(list: List<ActStudyItemUiModel>): List<ActStudyItemUiModel> {
        val sorted = list.sortedBy { it.week }

        var prevPassed = true
        return sorted.mapIndexed { index, item ->
            val locked = if (index == 0) false else !prevPassed

            prevPassed = (item.status == StudyStatus.PASS)

            item.copy(
                isLocked = locked,
                isExpanded = if (locked) false else item.isExpanded
            )
        }
    }
}
