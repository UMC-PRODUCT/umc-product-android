package com.umc.presentation.community.model

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