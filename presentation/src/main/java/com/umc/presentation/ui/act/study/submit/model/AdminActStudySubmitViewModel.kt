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
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.workbook.GetChallengerWorkbookSubmissionUseCase
import com.umc.domain.util.resolveAdminScope



@HiltViewModel
class AdminActStudySubmitViewModel @Inject constructor(
    private val getWorkbookSubmissionsUseCase: GetWorkbookSubmissionsUseCase,
    private val getStudyGroupsUseCase: GetStudyGroupsUseCase,
    private val getAvailableWeeksUseCase: GetAvailableWeeksUseCase,
    private val selectBestWorkbookUseCase: SelectBestWorkbookUseCase,
    private val reviewWorkbookUseCase: ReviewWorkbookUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getChallengerWorkbookSubmissionUseCase: GetChallengerWorkbookSubmissionUseCase,
) : BaseViewModel<AdminActStudySubmitState, AdminActStudySubmitEvent>(
    AdminActStudySubmitState()
) {

    init {
        loadFilterData()
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
                fetchSubmissionUrlAndOpenReview(action.item)
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

    private fun fetchSubmissionUrlAndOpenReview(item: AdminActStudySubmitItemUiModel) {
        viewModelScope.launch {
            when (val res = getChallengerWorkbookSubmissionUseCase(item.challengerWorkbookId)) {
                is ApiState.Success -> {
                    val url = res.data.submission.orEmpty()
                    val newItem = item.copy(submitUrl = url)

                    updateState { copy(reviewDialogTarget = newItem) }
                    emitEvent(AdminActStudySubmitEvent.ShowReviewDialog(newItem))
                }

                is ApiState.Fail -> {
                    emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
                    updateState { copy(reviewDialogTarget = item) }
                    emitEvent(AdminActStudySubmitEvent.ShowReviewDialog(item))
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
            startLoading()

            val scope = when (val meRes = getMyProfileUseCase()) {
                is ApiState.Success -> meRes.data.resolveAdminScope()
                is ApiState.Fail -> {
                    emitEvent(AdminActStudySubmitEvent.ShowToast(meRes.failState.message))
                    stopLoading()
                    return@launch
                }
            }

            if (scope == null) {
                emitEvent(AdminActStudySubmitEvent.ShowToast("어드민 권한 정보(파트/학교)를 찾을 수 없어요."))
                stopLoading()
                return@launch
            }

            when (val res = getStudyGroupsUseCase(scope.schoolId, scope.part)) {
                is ApiState.Success -> updateState { copy(studyGroups = res.data) }
                is ApiState.Fail -> emitEvent(AdminActStudySubmitEvent.ShowToast(res.failState.message))
            }

            when (val res = getAvailableWeeksUseCase()) {
                is ApiState.Success -> {
                    val weeks = res.data
                    val defaultWeek = weeks.firstOrNull() ?: 1
                    updateState { copy(availableWeeks = weeks, selectedWeek = defaultWeek) }
                    loadWorkbookSubmissions(reset = true)
                }
                is ApiState.Fail -> {
                    updateState { copy(selectedWeek = 1) }
                    loadWorkbookSubmissions(reset = true)
                }
            }
        }
    }


    fun loadWorkbookSubmissions(reset: Boolean) {
        startLoading()
        viewModelScope.launch {
            val state = uiState.value
            val cursor = if (reset) null else state.nextCursor

            val res = getWorkbookSubmissionsUseCase(
                weekNo = state.selectedWeek,
                studyGroupId = state.selectedGroupId,
                cursor = cursor,
                size = 20
            )

            resultResponse(
                response = res,
                successCallback = { page ->
                    val newItems = page.content
                        .filter { it.status in listOf("SUBMITTED", "PASS", "FAIL", "BEST") }
                        .map { it.toUiModel(state.selectedWeek) }

                    updateState {
                        copy(
                            items = if (reset) newItems else items + newItems,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
                        )
                    }
                },
                errorCallback = { fail ->
                    emitEvent(AdminActStudySubmitEvent.ShowToast(fail.message ?: "조회 실패"))
                }
            )
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
