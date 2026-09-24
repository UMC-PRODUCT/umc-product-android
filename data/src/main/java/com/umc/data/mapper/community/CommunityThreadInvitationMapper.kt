package com.umc.data.mapper.community

import com.umc.data.response.community.CommunityThreadInvitationResponse
import com.umc.data.response.community.CommunityThreadInvitablePageResponse
import com.umc.data.response.community.CommunityThreadInvitableResponse
import com.umc.data.response.community.CommunityThreadMemberResponse
import com.umc.domain.model.community.CommunityInvitableMember
import com.umc.domain.model.community.CommunityInvitableMemberPage
import com.umc.domain.model.community.CommunityThreadInvitation
import com.umc.domain.model.community.CommunityThreadMember

fun CommunityThreadInvitablePageResponse.toDomain():
        CommunityInvitableMemberPage {
    return CommunityInvitableMemberPage(
        items = items.map { item ->
            item.toDomain()
        },
        nextOffset = nextOffset,
        total = total.toIntOrNull() ?: 0,
    )
}

fun CommunityThreadInvitableResponse.toDomain():
        CommunityInvitableMember {
    return CommunityInvitableMember(
        memberId = memberId,
        challengerId = challengerId,
        name = name,
        part = part,
        generation = generation,
    )
}

fun CommunityThreadInvitationResponse.toDomain():
        CommunityThreadInvitation {
    return CommunityThreadInvitation(
        invitedMembers = invitedMembers.map { member ->
            member.toDomain()
        },
        memberCount = memberCount.toIntOrNull() ?: 0,
    )
}

fun CommunityThreadMemberResponse.toDomain():
        CommunityThreadMember {
    return CommunityThreadMember(
        memberId = memberId,
        name = name,
        // 파트가 없는 멤버(회장·운영진)가 있어도 목록 전체가 실패하지 않도록 빈 값으로 둔다
        part = part.orEmpty(),
        generation = generation.orEmpty(),
        role = role,
        joinedAt = joinedAt,
        state = state,
    )
}