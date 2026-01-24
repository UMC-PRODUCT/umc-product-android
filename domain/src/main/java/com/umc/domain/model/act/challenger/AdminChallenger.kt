package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.UserPart

data class AdminChallenger(
    val id: Int,
    val name: String,
    val nickname: String,
    val generation: Int,
    val part: UserPart,
    val outCount: Int = 0,
    val warningCount: Int = 0
)