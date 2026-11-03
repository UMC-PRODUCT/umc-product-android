package com.umc.data.response.member

import com.google.gson.annotations.SerializedName

/** 회원 차단·차단 해제 결과 */
data class MemberBlockResponse(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("isBlocked") val isBlocked: Boolean,
)

/** 내가 차단한 회원 목록 */
data class BlockedMemberListResponse(
    @SerializedName("members") val members: List<BlockedMemberResponse>?,
)

data class BlockedMemberResponse(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("blockedAt") val blockedAt: String?,
)
