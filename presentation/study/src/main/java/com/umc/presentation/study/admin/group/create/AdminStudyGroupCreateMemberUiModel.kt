package com.umc.presentation.study.admin.group.create

/**
 * 관리자 스터디 그룹 생성/수정에서 사용하는 멤버 UI 모델
 *
 * 스터디원 및 파트장 선택 화면에서
 * 챌린저의 기본 정보와 프로필 이미지를 표시하기 위해 사용합니다.
 */
data class AdminStudyGroupCreateMemberUiModel(
    val id: Long,
    val name: String,
    val displayName: String,
    val partLabel: String,
    val school: String,

    // 사용자 프로필 이미지 URL
    val profileImageUrl: String? = null,
)