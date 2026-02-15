package com.umc.presentation.ui.act.study.submit.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionsUseCase
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminActStudySubmitViewModel @Inject constructor(
    private val getWorkbookSubmissionsUseCase: GetWorkbookSubmissionsUseCase,
) : BaseViewModel<AdminActStudySubmitState, AdminActStudySubmitEvent>(
    AdminActStudySubmitState()
) {

    fun onAction(action: AdminActStudySubmitAction) {
        when (action) {
            is AdminActStudySubmitAction.SelectWeek -> {
                updateState { copy(selectedWeek = action.week) }
                loadWorkbookSubmissions(reset = true)
            }

            is AdminActStudySubmitAction.SelectGroupName -> {

                updateState { copy(selectedGroupName = action.name) }


                val mappedId = mapGroupNameToId(action.name)
                updateState { copy(selectedGroupId = mappedId) }

                loadWorkbookSubmissions(reset = true)
            }

            is AdminActStudySubmitAction.SelectGroup -> {
                updateState { copy(selectedGroupId = action.groupId) }
                loadWorkbookSubmissions(reset = true)
            }

            is AdminActStudySubmitAction.ClickBest -> {
                updateState { copy(bestDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowBestDialog(action.item))
            }

            is AdminActStudySubmitAction.ClickReview -> {
                updateState { copy(reviewDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowReviewDialog(action.item))
            }

            AdminActStudySubmitAction.DismissBestDialog ->
                updateState { copy(bestDialogTarget = null) }

            AdminActStudySubmitAction.DismissReviewDialog ->
                updateState { copy(reviewDialogTarget = null) }

            is AdminActStudySubmitAction.ConfirmBest -> {
                // TODO: 베스트 설정 API 생기면 연결
                emitEvent(AdminActStudySubmitEvent.ShowToast("베스트로 설정했어요."))
                updateState { copy(bestDialogTarget = null) }
            }

            is AdminActStudySubmitAction.SubmitReview -> {
                // TODO: PASS/FAIL 처리 API 생기면 연결
                emitEvent(
                    AdminActStudySubmitEvent.ShowToast(
                        if (action.pass) "통과 처리했어요." else "반려 처리했어요."
                    )
                )
                updateState { copy(reviewDialogTarget = null) }
            }
        }
    }

    fun loadWorkbookSubmissions(reset: Boolean) {
        viewModelScope.launch {
            val state = uiState.value
            val cursor = if (reset) null else state.nextCursor

            when (val res = getWorkbookSubmissionsUseCase(
                weekNo = state.selectedWeek,
                studyGroupId = state.selectedGroupId,
                cursor = cursor,
                size = 20
            )) {
                is ApiState.Success -> {
                    val page = res.data
                    val newItems = page.content
                        .filter { it.status == "SUBMITTED" }
                        .map { it.toUiModel(state.selectedWeek) }

                    updateState {
                        copy(
                            items = if (reset) newItems else items + newItems,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
                        )
                    }
                }

                is ApiState.Fail -> {
                    emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
                }
            }
        }
    }


    private fun mapGroupNameToId(name: String): Long? {
        return null // TODO: 그룹목록 API 붙이면 제거
    }
}
