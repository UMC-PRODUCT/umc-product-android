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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserStudyState(
    val title: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val items: List<NormalStudyItemUiModel> = emptyList(),
) : UiState {

    val totalCount: Int
        get() = items.size

    val passCount: Int
        get() = items.count { it.status == StudyStatus.PASS }

    val progress: Int
        get() = if (totalCount == 0) {
            0
        } else {
            passCount * 100 / totalCount
        }

    val percentText: String
        get() = "$progress%"

    val subText: String
        get() = AppStrings.STUDY_COMPLETE_FORMAT.format(
            passCount,
            totalCount,
        )
}

sealed interface UserStudyEvent : UiEvent {

    data class ShowToast(
        val message: String,
    ) : UserStudyEvent
}

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
                        items = items,
                    )
                }

                stopLoading()
            }

            is ApiState.Fail -> {
                stopLoading()

                emitEvent(
                    UserStudyEvent.ShowToast(
                        message = result.failState.message,
                    )
                )
            }
        }
    }

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
                }
            )
        }
    }

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

    private fun WorkbookMissionType.toPlatformLabel(): String {
        return when (this) {
            WorkbookMissionType.LINK -> "Github"
            WorkbookMissionType.FILE -> "File"
            WorkbookMissionType.TEXT -> "Text"
            WorkbookMissionType.UNKNOWN -> "-"
        }
    }
}