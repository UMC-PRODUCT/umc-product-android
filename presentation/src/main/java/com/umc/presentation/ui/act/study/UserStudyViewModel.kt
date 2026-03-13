package com.umc.presentation.ui.act.study

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.usecase.curriculum.GetMyCurriculumProgressUseCase
import com.umc.domain.usecase.curriculum.SubmitChallengerWorkbookUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserStudyState(
    val title: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val items: List<ActStudyItemUiModel> = emptyList(),
) : UiState {

    val totalCount: Int get() = items.size
    val passCount: Int get() = items.count { it.status == StudyStatus.PASS }

    val progress: Int
        get() = if (totalCount == 0) 0 else (passCount * 100 / totalCount)

    val percentText: String get() = "$progress%"
    val subText: String get() = "${passCount}/${totalCount} 완료"
}

sealed interface UserStudyEvent : UiEvent {
    data class ShowToast(val message: String) : UserStudyEvent
}

@HiltViewModel
class UserStudyViewModel @Inject constructor(
    private val getMyStudyProgressUseCase: GetMyCurriculumProgressUseCase,
    private val submitChallengerWorkbookUseCase: SubmitChallengerWorkbookUseCase,
) : BaseViewModel<UserStudyState, UserStudyEvent>(UserStudyState()) {


    companion object {
        private const val USE_FAKE_SUBMIT = false
    }

    init {
        load()
    }


    fun onSubmitClick(itemId: Long, link: String) {
        val trimmed = link.trim()

        updateItem(itemId) { item ->
            if (item.isLocked) return@updateItem item
            if (item.status != StudyStatus.IN_PROGRESS) return@updateItem item

            when (item.submitState) {
                SubmitState.REQUESTED -> item // 이미 요청중이면 무시
                SubmitState.READY, SubmitState.IDLE -> {
                    if (trimmed.isBlank()) item
                    else item.copy(link = trimmed, submitState = SubmitState.CONFIRMING)
                }
                SubmitState.CONFIRMING -> item.copy(link = trimmed) // 확인 화면이면 링크만 갱신
            }
        }
    }


    fun onConfirmClick(itemId: Long) {
        val item = uiState.value.items.firstOrNull { it.id == itemId } ?: return
        if (item.isLocked) return
        if (item.submitState != SubmitState.CONFIRMING) return

        val link = item.link.trim()
        if (link.isBlank()) {
            emitEvent(UserStudyEvent.ShowToast("링크를 입력해주세요."))
            return
        }

        updateItem(itemId) { it.copy(submitState = SubmitState.REQUESTED, isExpanded = true) }

        if (item.id <= 0L) {
            updateItem(itemId) { it.copy(submitState = SubmitState.CONFIRMING) }
            emitEvent(UserStudyEvent.ShowToast("원본 워크북 ID가 없어 제출할 수 없습니다."))
            return
        }

        viewModelScope.launch {
            submitWorkbook(item.id, link)
        }
    }




    private suspend fun submitWorkbook(originalWorkbookId: Long, link: String) {
        if (USE_FAKE_SUBMIT) {
            delay(800)
            updateItem(originalWorkbookId) {
                it.copy(
                    status = StudyStatus.PASS,
                    submitState = SubmitState.IDLE,
                    isExpanded = false
                )
            }
            emitEvent(UserStudyEvent.ShowToast("제출 완료!"))
            return
        }

        startLoading()

        Log.d("UserStudy", "submit originalWorkbookId=$originalWorkbookId, link=$link")
        Log.d("UserStudy", "submit API call originalWorkbookId=$originalWorkbookId link=$link")

        when (val res = submitChallengerWorkbookUseCase(originalWorkbookId, link)) {
            is ApiState.Success -> {
                stopLoading()
                load()
                emitEvent(UserStudyEvent.ShowToast("제출 완료!"))
            }
            is ApiState.Fail -> {
                stopLoading()
                updateItem(originalWorkbookId) { it.copy(submitState = SubmitState.CONFIRMING) }
                emitEvent(UserStudyEvent.ShowToast(res.failState.message))
            }
        }
    }


    private fun load() {
        startLoading()
        viewModelScope.launch {
            when (val result = getMyStudyProgressUseCase()) {
                is ApiState.Success -> {
                    val data = result.data
                    Log.d("UserStudy", "workbooks size = ${data.workbooks.size}")

                    val uiItems = data.workbooks
                        .sortedBy { it.weekNo }
                        .map { wb ->

                            val isBest = wb.status == WorkbookStatus.BEST
                            val status = if (isBest) StudyStatus.PASS else wb.status.toStudyStatus()

                            Log.d(
                                "UserStudy",
                                "week=${wb.weekNo} originalWorkbookId=${wb.originalWorkbookId} state=${wb.status}"
                            )

                            ActStudyItemUiModel(
                                id = wb.originalWorkbookId,
                                platform = wb.missionType.toPlatformText(),
                                title = wb.title,
                                status = status,
                                isBest = false, // 다음주 수정 부분
                                week = wb.weekNo,
                                submitState = wb.status.toSubmitState(),
                                isExpanded = false,
                                link = "",
                                description = wb.description,
                                isLocked = !wb.isReleased,
                            )
                        }

                    updateState {
                        copy(
                            title = data.curriculumTitle,
                            part = data.part,
                            items = applyLockRule(uiItems)
                        )
                    }

                    stopLoading()
                }

                is ApiState.Fail -> {
                    stopLoading()
                    emitEvent(UserStudyEvent.ShowToast(result.failState.message))
                }
            }
        }
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
        return list.sortedBy { it.week }
            .map { item ->
                item.copy(
                    isExpanded = if (item.isLocked) false else item.isExpanded
                )
            }
    }


    private fun WorkbookStatus.toStudyStatus(): StudyStatus = when (this) {
        WorkbookStatus.PASS -> StudyStatus.PASS
        WorkbookStatus.FAIL -> StudyStatus.FAIL
        WorkbookStatus.BEST -> StudyStatus.PASS
        WorkbookStatus.PENDING,
        WorkbookStatus.IN_PROGRESS,
        WorkbookStatus.SUBMITTED,
        WorkbookStatus.UNKNOWN -> StudyStatus.IN_PROGRESS
    }

    private fun WorkbookStatus.toSubmitState(): SubmitState = when (this) {
        WorkbookStatus.SUBMITTED -> SubmitState.REQUESTED
        WorkbookStatus.PASS,
        WorkbookStatus.FAIL,
        WorkbookStatus.BEST -> SubmitState.IDLE
        WorkbookStatus.PENDING,
        WorkbookStatus.IN_PROGRESS,
        WorkbookStatus.UNKNOWN -> SubmitState.READY
    }

    private fun WorkbookMissionType.toPlatformText(): String = when (this) {
        WorkbookMissionType.LINK -> "Link"
        WorkbookMissionType.FILE -> "File"
        WorkbookMissionType.TEXT -> "Text"
        WorkbookMissionType.UNKNOWN -> "Unknown"
    }
}
