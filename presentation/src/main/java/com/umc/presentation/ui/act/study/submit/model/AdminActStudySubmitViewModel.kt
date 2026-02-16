package com.umc.presentation.ui.act.study.submit.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.GetAvailableWeeksUseCase
import com.umc.domain.usecase.curriculum.GetStudyGroupsUseCase
import com.umc.domain.usecase.curriculum.GetWorkbookSubmissionsUseCase
import com.umc.domain.usecase.workbook.ReviewWorkbookUseCase
import com.umc.domain.usecase.workbook.SelectBestWorkbookUseCase
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class AdminActStudySubmitViewModel @Inject constructor(
    private val getWorkbookSubmissionsUseCase: GetWorkbookSubmissionsUseCase,
    private val getStudyGroupsUseCase: GetStudyGroupsUseCase,
    private val getAvailableWeeksUseCase: GetAvailableWeeksUseCase,
    private val selectBestWorkbookUseCase: SelectBestWorkbookUseCase,
    private val reviewWorkbookUseCase: ReviewWorkbookUseCase,
) : BaseViewModel<AdminActStudySubmitState, AdminActStudySubmitEvent>(
    AdminActStudySubmitState()
) {

    init {
        loadFilterData()
        loadWorkbookSubmissions(reset = true)
    }

    fun onAction(action: AdminActStudySubmitAction) {
        when (action) {
            is AdminActStudySubmitAction.SelectWeek -> {
                updateState { copy(selectedWeek = action.week) }
                loadWorkbookSubmissions(reset = true)
            }

            is AdminActStudySubmitAction.SelectGroupName -> {
                updateState { copy(selectedGroupId = mapGroupNameToId(action.name)) }
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
                val target = uiState.value.bestDialogTarget ?: return

                viewModelScope.launch {
                    when (val res = selectBestWorkbookUseCase(
                        challengerWorkbookId = target.challengerWorkbookId,
                        reason = action.reason
                    )) {
                        is ApiState.Success -> {
                            emitEvent(AdminActStudySubmitEvent.ShowToast("베스트로 설정했어요."))
                            updateState { copy(bestDialogTarget = null) }
                            loadWorkbookSubmissions(reset = true)
                        }
                        is ApiState.Fail -> {
                            emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
                        }
                    }
                }
            }


            is AdminActStudySubmitAction.SubmitReview -> {
                val target = uiState.value.reviewDialogTarget ?: return

                val status = if (action.pass) "PASS" else "FAIL"

                viewModelScope.launch {
                    when (val res = reviewWorkbookUseCase(
                        challengerWorkbookId = target.challengerWorkbookId,
                        status = status,
                        feedback = action.feedback // 있으면, 없으면 null
                    )) {
                        is ApiState.Success -> {
                            emitEvent(AdminActStudySubmitEvent.ShowToast(if (action.pass) "통과 처리했어요." else "반려 처리했어요."))
                            updateState { copy(reviewDialogTarget = null) }
                            loadWorkbookSubmissions(reset = true)
                        }
                        is ApiState.Fail -> {
                            emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
                        }
                    }
                }
            }

        }
    }

    fun getGroupNames(state: AdminActStudySubmitState): List<String> {
        return listOf("전체 그룹") + state.studyGroups.map { group ->
            group.name
        }
    }


    fun getSelectedGroupName(state: AdminActStudySubmitState?): String {
        val safeState = state ?: return "전체 그룹"
        val id = safeState.selectedGroupId ?: return "전체 그룹"
        return safeState.studyGroups.firstOrNull { it.id == id }?.name ?: "전체 그룹"
    }




    private fun loadFilterData() {
        viewModelScope.launch {
            val schoolId = 18L
            val part = "ANDROID"

            when (val res = getStudyGroupsUseCase(schoolId, part)) {
                is ApiState.Success -> updateState { copy(studyGroups = res.data) }
                is ApiState.Fail -> emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
            }

            when (val res = getAvailableWeeksUseCase()) {
                is ApiState.Success -> updateState { copy(availableWeeks = res.data) }
                is ApiState.Fail -> Unit
            }
        }
    }

    fun loadWorkbookSubmissions(reset: Boolean) {
        viewModelScope.launch {
            val state = uiState.value
            val cursor = if (reset) null else state.nextCursor

            when (
                val res = getWorkbookSubmissionsUseCase(
                    weekNo = state.selectedWeek,
                    studyGroupId = state.selectedGroupId,
                    cursor = cursor,
                    size = 20
                )
            ) {
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
        if (name == "전체 그룹") return null

        val group = uiState.value.studyGroups.firstOrNull { group ->
            group.name == name
        }

        return group?.id
    }

}
