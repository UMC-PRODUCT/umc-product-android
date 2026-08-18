package com.umc.presentation.community.model

/**
 * 커뮤니티에서 챌린저 정보를 화면에 표시하기 위한 UI 모델
 *
 * 챌린저의 기본 정보와 프로필 이미지를 관리하며,
 * 이름과 닉네임을 조합한 표시용 이름을 제공합니다.
 */
data class CommunityChallengerUiModel(
    val memberId: Long,
    val name: String,
    val nickname: String,
    val school: String,
    val generation: Long,
    val partLabel: String,
    val profileImage: String = "",
) {
    val displayName: String
        get() = if (nickname.isBlank()) {
            name
        } else {
            "$name/$nickname"
        }
}