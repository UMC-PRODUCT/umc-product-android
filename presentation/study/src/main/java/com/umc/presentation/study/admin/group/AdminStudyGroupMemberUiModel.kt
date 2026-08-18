package com.umc.presentation.study.admin.group

/**
 * 관리자 스터디 그룹 카드에서
 * 개별 스터디원을 표시하기 위한 UI 모델
 *
 * 스터디원 Chip 및 멤버 목록에서
 * 이름, 학교, 프로필 이미지를 표시하는 데 사용합니다.
 */
data class AdminStudyGroupMemberUiModel(

    /**
     * 스터디원 식별 ID
     *
     * 현재 API 구조에서는 memberId 값을 사용합니다.
     */
    val challengerId: Long,

    /** 스터디원 이름 */
    val name: String,

    /** 스터디원 학교 */
    val school: String = "",

    /** 스터디원 프로필 이미지 URL */
    val profileImageUrl: String? = null,
)