package com.umc.presentation.study.normal

/**
 * 일반 사용자 스터디 화면에서 사용하는 주차별 커리큘럼 UI 모델
 *
 * 주차 정보와 제목·설명, 공개(잠금) 여부만 관리합니다.
 */
data class NormalStudyItemUiModel(
    val id: Long,
    val title: String,
    val week: Int,
    val description: String,
    val isLocked: Boolean = false,
)
