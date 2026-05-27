package com.umc.presentation.study.normal

import androidx.lifecycle.viewModelScope
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.base.BaseViewModel
import com.umc.component.theme.AppStrings
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.usecase.curriculum.GetMyCurriculumProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


data class UserStudyState(
    val title: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val items: List<NormalStudyItemUiModel> = emptyList(),
) : UiState {
    val totalCount: Int get() = items.size
    val passCount: Int get() = items.count { it.status == StudyStatus.PASS }
    val progress: Int get() = if (totalCount == 0) 0 else (passCount * 100 / totalCount)
    val percentText: String get() = "$progress%"
    val subText: String get() = AppStrings.STUDY_COMPLETE_FORMAT.format(passCount, totalCount)
}


sealed interface UserStudyEvent : UiEvent {
    data class ShowToast(val message: String) : UserStudyEvent
}

@HiltViewModel
class UserStudyViewModel @Inject constructor(
    private val getMyStudyProgressUseCase: GetMyCurriculumProgressUseCase,
) : BaseViewModel<UserStudyState, UserStudyEvent>(UserStudyState()) {

    companion object {
        private const val USE_DUMMY = true

        // 하드코딩 제거 후 로그인 유저 정보에서 가져오기
        private const val GISU_ID = 3L
        private const val PART = "SPRINGBOOT"
    }

    init {
        if (USE_DUMMY) loadDummy() else load()
    }

    // TODO: 더미 데이터 제거 - 실제 API 연결 후 삭제
    private fun loadDummy() {
        updateState {
            copy(
                title = "10기 SpringBoot",
                items = listOf(
                    NormalStudyItemUiModel(
                        id = 1L,
                        week = 1,
                        platform = "Github",
                        title = "1주차 - Spring Boot 시작하기",
                        description = "Spring Boot 기초 개념과 프로젝트 구조를 학습합니다.",
                        status = StudyStatus.PASS,
                        submitState = SubmitState.IDLE,
                        isLocked = false,
                        isBest = true,
                    ),
                    NormalStudyItemUiModel(
                        id = 2L,
                        week = 2,
                        platform = "Github",
                        title = "2주차 - JPA 기초",
                        description = "JPA와 Hibernate를 이용한 데이터베이스 연동을 학습합니다.",
                        status = StudyStatus.FAIL,
                        submitState = SubmitState.IDLE,
                        isLocked = false,
                    ),
                    NormalStudyItemUiModel(
                        id = 3L,
                        week = 3,
                        platform = "Github",
                        title = "3주차 - Spring Security",
                        description = "Spring Security를 이용한 인증/인가를 학습합니다.",
                        status = StudyStatus.IN_PROGRESS,
                        submitState = SubmitState.READY,
                        isLocked = false,
                    ),
                    NormalStudyItemUiModel(
                        id = 4L,
                        week = 4,
                        platform = "Github",
                        title = "4주차 - REST API 설계",
                        description = "REST API 설계 원칙과 구현 방법을 학습합니다.",
                        status = StudyStatus.IN_PROGRESS,
                        submitState = SubmitState.IDLE,
                        isLocked = true,
                    ),
                    NormalStudyItemUiModel(
                        id = 5L,
                        week = 5,
                        platform = "Github",
                        title = "5주차 - 테스트 코드",
                        description = "JUnit과 Mockito를 이용한 테스트 코드 작성을 학습합니다.",
                        status = StudyStatus.IN_PROGRESS,
                        submitState = SubmitState.IDLE,
                        isLocked = true,
                    ),
                )
            )
        }
    }

    /** 커리큘럼 개요 로드 */
    private fun load() {
        startLoading()
        viewModelScope.launch {
            when (val resultResponse = getMyStudyProgressUseCase(
                // TODO: 하드코딩 제거 후 로그인 유저 정보에서 gisuId, part 가져오기..
                gisuId = GISU_ID,
                part = PART
            )) {
                is ApiState.Success -> {
                    val data = resultResponse.data
                    val uiItems = data.workbooks
                        .sortedBy { it.weekNo }
                        .map { wb ->
                            NormalStudyItemUiModel(
                                id = wb.originalWorkbookId,
                                platform = "Github",
                                title = wb.title,
                                status = when (wb.status) {
                                    WorkbookStatus.PASS, WorkbookStatus.BEST -> StudyStatus.PASS
                                    WorkbookStatus.FAIL -> StudyStatus.FAIL
                                    WorkbookStatus.PENDING,
                                    WorkbookStatus.IN_PROGRESS,
                                    WorkbookStatus.SUBMITTED,
                                    WorkbookStatus.UNKNOWN -> StudyStatus.IN_PROGRESS
                                },
                                week = wb.weekNo,
                                submitState = SubmitState.IDLE,
                                isExpanded = false,
                                link = "",
                                description = "",
                                isLocked = !wb.isReleased,
                                isBest = false,
                            )
                        }
                    updateState {
                        copy(
                            title = data.curriculumTitle,
                            part = data.part,
                            items = uiItems
                        )
                    }
                    stopLoading()
                }
                is ApiState.Fail -> {
                    stopLoading()
                    emitEvent(UserStudyEvent.ShowToast(resultResponse.failState.message))
                }
            }
        }
    }

    /** 카드 펼치기/접기 토글 */
    fun toggleExpand(index: Int) {
        updateState {
            val target = items.getOrNull(index) ?: return@updateState this
            if (target.isLocked) return@updateState this
            val updated = items.mapIndexed { i, item ->
                if (i == index) item.copy(isExpanded = !item.isExpanded) else item
            }
            copy(items = updated)
        }
    }

    /**
     * 링크 제출 버튼 클릭
     * - 링크 입력 후 CONFIRMING 상태로 전환
     * TODO: 실제 API 연결 후 submitChallengerWorkbookUseCase 호출로 교체
     */
    fun onSubmitClick(itemId: Long, link: String) {
        val trimmed = link.trim()
        if (trimmed.isBlank()) {
            emitEvent(UserStudyEvent.ShowToast("링크를 입력해주세요."))
            return
        }
        updateItem(itemId) { item ->
            if (item.isLocked) return@updateItem item
            if (item.status != StudyStatus.IN_PROGRESS) return@updateItem item
            when (item.submitState) {
                SubmitState.READY, SubmitState.IDLE ->
                    item.copy(link = trimmed, submitState = SubmitState.CONFIRMING)
                SubmitState.CONFIRMING ->
                    item.copy(link = trimmed)
                else -> item
            }
        }
    }

    /**
     * 학습 완료 인증 버튼 클릭
     * - CONFIRMING → REQUESTED 로 전환
     * TODO: 실제 API 연결 후 서버에 제출 요청으로 교체
     */
    fun onConfirmClick(itemId: Long) {
        val item = uiState.value.items.firstOrNull { it.id == itemId } ?: return
        if (item.submitState != SubmitState.CONFIRMING) return

        // TODO: 실제 API 연결 시 로직으로 교체
        // viewModelScope.launch { submitWorkbook(item.id, item.link) }

        // 임시: 바로 REQUESTED 상태로 변경
        updateItem(itemId) {
            it.copy(submitState = SubmitState.REQUESTED, isExpanded = true)
        }
        emitEvent(UserStudyEvent.ShowToast("제출 완료!"))
    }

    private fun updateItem(
        itemId: Long,
        transform: (NormalStudyItemUiModel) -> NormalStudyItemUiModel
    ) {
        updateState {
            val updated = items.map { if (it.id == itemId) transform(it) else it }
            copy(items = updated)
        }
    }

    // TODO: 실제 API 연결 시 활성화
    /*
    private suspend fun submitWorkbook(originalWorkbookId: Long, link: String) {
        startLoading()
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
    */
}