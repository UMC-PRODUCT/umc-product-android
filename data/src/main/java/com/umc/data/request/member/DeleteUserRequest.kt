package com.umc.data.request.member

data class DeleteUserRequest(
    val googleAccessToken : String,
    val kakaoAccessToken : String
)
