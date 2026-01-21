package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart

data class UserChallenger(
    val id: Int,
    val name: String,
    val nickname: String,
    val generation: Int,
    val part: UserPart,
    val role: UserChallengerRole = UserChallengerRole.MEMBER,
    val profileImage: String? = null
)