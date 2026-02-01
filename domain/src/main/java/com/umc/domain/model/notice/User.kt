package com.umc.domain.model.notice

data class User(
    val id: Int,
    val name: String,
    val nickName: String,
    val branch: String,
    val school: String,
    val part: String,
    val isSendNotification: Boolean,
    val isCheck: Boolean
)
