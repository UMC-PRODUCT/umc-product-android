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
) : BaseViewModel<UserStudyState, UserStudyEvent>(UserStudyState()) {


    companion object {
        private const val USE_FAKE_SUBMIT = true
    }


    init {
        loadFake()
    }

    private fun loadFake() {
        val items = listOf(
            ActStudyItemUiModel(
                id = 1L,
                platform = "Link",
                title = "Kotlin 기초",
                status = StudyStatus.PASS,
                week = 1,
                isExpanded = false,
                link = "",
                submitState = SubmitState.IDLE,
                isLocked = false,
                description = "Kotlin 문법 이해",
            ),
            ActStudyItemUiModel(
                id = 2L, platform = "Link", title = "Android 기본 구조",
                status = StudyStatus.PASS, week = 2,
                submitState = SubmitState.IDLE, isLocked = false,
                description = "Activity 및 Lifecycle 이해",
            ),
            ActStudyItemUiModel(
                id = 3L, platform = "Link", title = "UI 구성",
                status = StudyStatus.FAIL, week = 3,
                submitState = SubmitState.IDLE, isLocked = false,
                description = "XML 레이아웃 설계",
            ),
            ActStudyItemUiModel(
                id = 4L, platform = "Link", title = "네트워크 통신",
                status = StudyStatus.IN_PROGRESS, week = 4,
                submitState = SubmitState.REQUESTED, isLocked = false,
                description = "Retrofit 활용",
            ),
            ActStudyItemUiModel(
                id = 5L, platform = "Link", title = "MVVM 패턴",
                status = StudyStatus.IN_PROGRESS, week = 5,
                submitState = SubmitState.READY, isLocked = false,
                description = "아키텍처 패턴 이해",
            ),
            // 6~10 잠금
            ActStudyItemUiModel(6L, "Link", "로컬 저장", StudyStatus.IN_PROGRESS, week = 6, submitState = SubmitState.READY, isLocked = true, description = "Room DB 활용"),
            ActStudyItemUiModel(7L, "Link", "인증 구현", StudyStatus.IN_PROGRESS, week = 7, submitState = SubmitState.READY, isLocked = true, description = "로그인 기능 구현"),
            ActStudyItemUiModel(8L, "Link", "배포 준비", StudyStatus.IN_PROGRESS, week = 8, submitState = SubmitState.READY, isLocked = true, description = "앱 서명 및 배포 과정 이해"),
            ActStudyItemUiModel(9L, "Link", "테스트", StudyStatus.IN_PROGRESS, week = 9, submitState = SubmitState.READY, isLocked = true, description = "Unit Test 작성"),
            ActStudyItemUiModel(10L, "Link", "리팩토링", StudyStatus.IN_PROGRESS, week = 10, submitState = SubmitState.READY, isLocked = true, description = "코드 구조 개선"),
        )

        updateState {
            copy(
                title = "9기 Android",
                part = UserPart.ANDROID,
                items = applyLockRule(items)
            )
        }

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
                    else item.copy(link = trimmed, submitState = SubmitState.CONFIRMING, isExpanded = true)
                }
                SubmitState.CONFIRMING -> item.copy(link = trimmed, isExpanded = true)
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


        updateItem(itemId) {
            it.copy(
                submitState = SubmitState.REQUESTED,
                isExpanded = true
            )
        }

        emitEvent(UserStudyEvent.ShowToast("제출 요청 완료! (검토 대기)"))
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
        val sorted = list.sortedBy { it.week.toString().toIntOrNull() ?: 0 }
        var prevEvaluated = true

        return sorted.mapIndexed { index, item ->
            val locked = if (index == 0) false else !prevEvaluated
            prevEvaluated =
                (item.status == StudyStatus.PASS || item.status == StudyStatus.FAIL || item.submitState == SubmitState.REQUESTED)


            item.copy(
                isLocked = locked,
                isExpanded = if (locked) false else item.isExpanded
            )
        }
    }


    private fun WorkbookStatus.toStudyStatus(): StudyStatus = when (this) {
        WorkbookStatus.PASS -> StudyStatus.PASS
        WorkbookStatus.FAIL -> StudyStatus.FAIL
        WorkbookStatus.PENDING,
        WorkbookStatus.IN_PROGRESS,
        WorkbookStatus.SUBMITTED,
        WorkbookStatus.UNKNOWN -> StudyStatus.IN_PROGRESS
    }

    private fun WorkbookStatus.toSubmitState(): SubmitState = when (this) {
        WorkbookStatus.SUBMITTED -> SubmitState.REQUESTED
        WorkbookStatus.PASS,
        WorkbookStatus.FAIL -> SubmitState.IDLE
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
