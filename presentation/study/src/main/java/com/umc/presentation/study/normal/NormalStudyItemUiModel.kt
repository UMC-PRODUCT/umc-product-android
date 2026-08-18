package com.umc.presentation.study.normal

import com.umc.domain.model.enums.StudyStatus

/**
 * 일반 사용자 스터디 화면에서 사용하는 주차별 커리큘럼 UI 모델
 *
 * 주차 정보, 제출 상태, 플랫폼, 잠금 여부,
 * BEST 여부와 펼침 상태 등을 관리합니다.
 */
data class NormalStudyItemUiModel(
    val id: Long,
    val title: String,
    val status: StudyStatus,
    val week: Int,
    val description: String,
    val platform: String = "Github",
    val isExpanded: Boolean = false,
    val isLocked: Boolean = false,
    val isBest: Boolean = false,
    val feedbackMessage: String? = null,
)