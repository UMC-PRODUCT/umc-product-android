package com.umc.domain.model.act.challenger

import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart

data class UserChallenger(
    val id: Long,
    val name: String,
    val nickname: String,
    val generation: Int,
    val part: UserPart,
    val role: UserChallengerRole = UserChallengerRole.MEMBER,
    val pointSum: Double,
    val profileImage: String? = null
)

data class ChallengerList(
    val challengers: List<UserChallenger>,
    val partCounts: List<UserPartCount>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

data class UserPartCount(
    val part: UserPart,
    val count: Int
)