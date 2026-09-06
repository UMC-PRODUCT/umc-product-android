package com.umc.presentation.study.normal

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.act.study.StudyProgress
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.usecase.curriculum.GetMyCurriculumProgressUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 일반 사용자 스터디 화면에서 사용하는 UI 상태
 *
 * 커리큘럼 제목, 파트, 주차별 항목과
 * 전체 진행률 정보를 관리합니다.
 */
data class UserStudyState(
    val title: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val items: ImmutableList<NormalStudyItemUiModel> = persistentListOf(),
) : UiState {

    /** 전체 커리큘럼 개수 */
    val totalCount: Int
        get() = items.size

    /** PASS 상태의 커리큘럼 개수 */
    val passCount: Int
        get() = items.count { it.status == StudyStatus.PASS }

    /** 전체 커리큘럼 기준 달성률 */
    val progress: Int
        get() = if (totalCount == 0) {
            0
        } else {
            passCount * 100 / totalCount
        }

    /** 화면에 표시할 퍼센트 문자열 */
    val percentText: String
        get() = "$progress%"

    /** 완료 개수 / 전체 개수 안내 문구 */
    val subText: String
        get() = AppStrings.STUDY_COMPLETE_FORMAT.format(
            passCount,
            totalCount,
        )
}

/**
 * 일반 사용자 스터디 화면에서 발생하는 일회성 UI 이벤트
 */
sealed interface UserStudyEvent : UiEvent {

    /** 사용자에게 Toast 메시지 표시 */
    data class ShowToast(
        val message: String,
    ) : UserStudyEvent
}

/**
 * 일반 사용자 스터디 화면의 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 현재 사용자 프로필 조회
 * - 현재 활성 기수 및 파트 확인
 * - 개인 커리큘럼 진행 현황 조회
 * - 워크북 상태를 화면 상태로 변환
 * - 주차별 항목 펼치기/접기
 * - 로딩 및 Toast 이벤트 처리
 */
@HiltViewModel
class UserStudyViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getMyCurriculumProgressUseCase: GetMyCurriculumProgressUseCase,
) : BaseViewModel<UserStudyState, UserStudyEvent>(
    UserStudyState(),
) {

    init {
        load()
    }

    /**
     * 현재 사용자 정보를 조회한 뒤
     * 현재 활성 기수와 파트를 기준으로 커리큘럼 진행 현황을 조회합니다.
     */
    private fun load() {
        startLoading()

        viewModelScope.launch {
            when (val profileResult = getMyProfileUseCase()) {
                is ApiState.Success<*> -> {
                    val userInfo = profileResult.data as? UserInfo

                    if (userInfo == null) {
                        stopLoading()

                        emitEvent(
                            UserStudyEvent.ShowToast(
                                message = "사용자 정보 형식이 올바르지 않아요.",
                            )
                        )
                        return@launch
                    }

                    /**
                     * ACTIVE 상태의 가장 최신 챌린저 기록을 우선 사용하고,
                     * 없다면 가장 최근 기수 기록을 사용합니다.
                     */
                    val currentRecord = userInfo.challengerRecords
                        .filter { record ->
                            record.challengerStatus == "ACTIVE"
                        }
                        .maxByOrNull { record ->
                            record.gisu
                        }
                        ?: userInfo.challengerRecords.maxByOrNull { record ->
                            record.gisu
                        }

                    if (currentRecord == null) {
                        stopLoading()

                        emitEvent(
                            UserStudyEvent.ShowToast(
                                message = "챌린저 기수 정보를 찾을 수 없어요.",
                            )
                        )
                        return@launch
                    }

                    // 서버의 파트 문자열을 UserPart enum으로 변환
                    val part = UserPart.from(currentRecord.part)

                    loadStudyProgress(
                        gisuId = currentRecord.gisuId,
                        part = part,
                    )
                }

                is ApiState.Fail -> {
                    stopLoading()

                    emitEvent(
                        UserStudyEvent.ShowToast(
                            message = profileResult.failState.message,
                        )
                    )
                }
            }
        }
    }

    /**
     * 현재 기수의 개인 커리큘럼 진행 현황을 조회합니다.
     *
     * 서버 워크북 데이터를 주차 순으로 정렬한 뒤
     * 화면에서 사용하는 NormalStudyItemUiModel로 변환합니다.
     */
    private suspend fun loadStudyProgress(
        gisuId: Long,
        part: UserPart,
    ) {
        when (
            val result = getMyCurriculumProgressUseCase(
                gisuId = gisuId,
            )
        ) {
            is ApiState.Success<*> -> {
                val data = result.data as? StudyProgress

                if (data == null) {
                    stopLoading()

                    emitEvent(
                        UserStudyEvent.ShowToast(
                            message = "커리큘럼 데이터 형식이 올바르지 않아요.",
                        )
                    )
                    return
                }

                // 서버 워크북 목록을 화면 표시용 모델로 변환
                val items = data.workbooks
                    .sortedBy { workbook ->
                        workbook.weekNo
                    }
                    .map { workbook ->
                        NormalStudyItemUiModel(
                            id = workbook.originalWorkbookId,
                            title = workbook.title,
                            status = workbook.status.toStudyStatus(),
                            week = workbook.weekNo,
                            description = workbook.description,
                            platform = workbook.missionType.toPlatformLabel(),
                            isLocked = !workbook.isReleased,
                            isBest = workbook.status == WorkbookStatus.BEST,
                        )
                    }

                updateState {
                    copy(
                        title = data.curriculumTitle,
                        part = part,
                        items = items.toImmutableList(),
                    )
                }

                stopLoading()
            }

            is ApiState.Fail -> {
                stopLoading()

                // API 실패 시 빈 화면으로 유지
            }
        }
    }

    /**
     * 선택한 주차의 상세 영역을 펼치거나 접습니다.
     *
     * 잠긴 항목은 상태를 변경하지 않습니다.
     */
    fun toggleExpand(index: Int) {
        updateState {
            val target = items.getOrNull(index)
                ?: return@updateState this

            if (target.isLocked) {
                return@updateState this
            }

            copy(
                items = items.mapIndexed { itemIndex, item ->
                    if (itemIndex == index) {
                        item.copy(
                            isExpanded = !item.isExpanded,
                        )
                    } else {
                        item
                    }
                }.toImmutableList()
            )
        }
    }

    /**
     * 서버 WorkbookStatus를 화면에서 사용하는 StudyStatus로 변환합니다.
     */
    private fun WorkbookStatus.toStudyStatus(): StudyStatus {
        return when (this) {
            WorkbookStatus.PASS,
            WorkbookStatus.BEST,
                -> StudyStatus.PASS

            WorkbookStatus.FAIL ->
                StudyStatus.FAIL

            WorkbookStatus.PENDING,
            WorkbookStatus.IN_PROGRESS,
            WorkbookStatus.SUBMITTED,
            WorkbookStatus.UNKNOWN,
                -> StudyStatus.IN_PROGRESS
        }
    }

    /**
     * 워크북 제출 방식에 따라 화면에 표시할 플랫폼 문자열로 변환합니다.
     */
    private fun WorkbookMissionType.toPlatformLabel(): String {
        return when (this) {
            WorkbookMissionType.LINK -> "Github"
            WorkbookMissionType.FILE -> "File"
            WorkbookMissionType.TEXT -> "Text"
            WorkbookMissionType.UNKNOWN -> "-"
        }
    }
}