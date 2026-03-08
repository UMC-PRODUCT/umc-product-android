package com.umc.domain.model.mypage

data class UserOAuthType(
    val memberOAuthId: Long,
    val memberId: Long,
    val provider: String
)
