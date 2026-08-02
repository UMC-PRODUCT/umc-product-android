package com.umc.data.mapper.community

import com.umc.data.response.community.CommunityThreadMemberPageResponse
import com.umc.data.response.community.CommunityThreadMemberResponse
import com.umc.domain.model.community.CommunityThreadMember
import com.umc.domain.model.community.CommunityThreadMemberPage

fun CommunityThreadMemberPageResponse.toDomain():
        CommunityThreadMemberPage {
    return CommunityThreadMemberPage(
        items = items.map { member ->
            member.toDomain()
        },
        nextOffset = nextOffset,
        total = total.toIntOrNull() ?: 0,
    )
}

