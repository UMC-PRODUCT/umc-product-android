package com.umc.data.response.community

data class CommunityThreadMemberResponse(
    val memberId: String,
    val name: String,
    // 회장·운영진처럼 파트가 없는 멤버는 null 로 내려온다. 기수도 같은 이유로 비어 있을 수 있다.
    val part: String?,
    val generation: String?,
    val role: String,
    val joinedAt: String,
    val state: String,
)