package com.umc.presentation.study

import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.UserPart


/**
 * 스터디/활동 화면 전체 UI 상태
 *
 * @param title 커리큘럼 제목
 * @param part 사용자 파트 (WEB, SERVER 등)
 * @param items 주차별 스터디 아이템 목록
 */
data class UserStudyState(
    val title: String = "",
    val part: UserPart = UserPart.UNKNOWN,
    val items: List<ActStudyItemUiModel> = emptyList(),
) : UiState {

    /** 전체 주차 수 */
    val totalCount: Int get() = items.size

    /** 통과한 주차 수 */
    val passCount: Int get() = items.count { it.status == StudyStatus.PASS }

    /** 달성률 (0~100) */
    val progress: Int get() = if (totalCount == 0) 0 else (passCount * 100 / totalCount)

    /** 달성률 텍스트 (예: "25%") */
    val percentText: String get() = "$progress%"

    /** 완료 현황 텍스트 (예: "2/8 완료") */
    val subText: String get() = AppStrings.STUDY_COMPLETE_FORMAT.format(passCount, totalCount)
}