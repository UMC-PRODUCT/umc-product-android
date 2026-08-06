package com.umc.data.mapper.community

import com.umc.data.response.community.CommunityThreadDetailResponse
import com.umc.data.response.community.CommunityThreadInvitationResponse
import com.umc.data.response.community.CommunityThreadInvitablePageResponse
import com.umc.data.response.community.CommunityThreadInvitableResponse
import com.umc.data.response.community.CommunityThreadLastMessageResponse
import com.umc.data.response.community.CommunityThreadListResponse
import com.umc.data.response.community.CommunityThreadMemberMutationResponse
import com.umc.data.response.community.CommunityThreadMemberPageResponse
import com.umc.data.response.community.CommunityThreadMemberResponse
import com.umc.data.response.community.CommunityThreadSummaryResponse
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadInvitable
import com.umc.domain.model.community.thread.CommunityThreadInvitablePage
import com.umc.domain.model.community.thread.CommunityThreadInvitation
import com.umc.domain.model.community.thread.CommunityThreadLastMessage
import com.umc.domain.model.community.thread.CommunityThreadList
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMemberMutation
import com.umc.domain.model.community.thread.CommunityThreadMemberPage
import com.umc.domain.model.community.thread.CommunityThreadMemberState
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.community.thread.CommunityThreadSummary
import com.umc.domain.model.community.thread.CommunityThreadCategory

fun CommunityThreadListResponse.toChatDomain() = CommunityThreadList(
    pinned = pinned.map(CommunityThreadSummaryResponse::toChatDomain),
    threads = threads.map(CommunityThreadSummaryResponse::toChatDomain),
    nextOffset = nextOffset,
    total = total,
)

fun CommunityThreadSummaryResponse.toChatDomain() = CommunityThreadSummary(
    threadId = threadId,
    title = title,
    description = description,
    category = category.toChatCategory(),
    icon = icon,
    memberCount = memberCount,
    unreadCount = unreadCount,
    maxMembers = maxMembers,
    isPinned = isPinned,
    isMuted = isMuted,
    myRole = myRole.toChatRole(),
    lastMessage = lastMessage?.toChatDomain(),
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CommunityThreadDetailResponse.toChatDomain() = CommunityThreadDetail(
    threadId = threadId,
    title = title,
    description = description,
    category = category.toChatCategory(),
    icon = icon,
    memberCount = memberCount,
    unreadCount = unreadCount,
    maxMembers = maxMembers,
    isPinned = isPinned,
    isMuted = isMuted,
    myRole = myRole.toChatRole(),
    lastMessage = lastMessage?.toChatDomain(),
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    shareUrl = shareUrl,
    deletedAt = deletedAt,
)

fun CommunityThreadMemberPageResponse.toChatDomain() = CommunityThreadMemberPage(
    items = items.map(CommunityThreadMemberResponse::toChatDomain),
    nextOffset = nextOffset,
    total = total,
)

fun CommunityThreadInvitablePageResponse.toChatDomain() = CommunityThreadInvitablePage(
    items = items.map(CommunityThreadInvitableResponse::toChatDomain),
    nextOffset = nextOffset,
    total = total,
)

fun CommunityThreadInvitationResponse.toChatDomain() = CommunityThreadInvitation(
    invitedMembers = invitedMembers.map(CommunityThreadMemberResponse::toChatDomain),
    memberCount = memberCount,
)

fun CommunityThreadMemberMutationResponse.toChatDomain() = CommunityThreadMemberMutation(
    threadId = threadId,
    memberId = memberId,
    role = role.toChatRole(),
    state = state.toChatMemberState(),
    memberCount = memberCount,
)

private fun CommunityThreadMemberResponse.toChatDomain() = CommunityThreadMember(
    memberId = memberId,
    name = name,
    part = part,
    generation = generation,
    role = role.toChatRole(),
    joinedAt = joinedAt,
    state = state.toChatMemberState(),
)

private fun CommunityThreadInvitableResponse.toChatDomain() = CommunityThreadInvitable(
    memberId = memberId,
    challengerId = challengerId,
    name = name,
    part = part,
    generation = generation,
)

private fun CommunityThreadLastMessageResponse.toChatDomain() = CommunityThreadLastMessage(
    preview = preview,
    senderName = senderName,
    createdAt = createdAt,
)

private fun String.toChatCategory() =
    CommunityThreadCategory.entries.firstOrNull { it.name == uppercase() }
        ?: CommunityThreadCategory.FREE

private fun String?.toChatRole() =
    CommunityThreadRole.entries.firstOrNull { it.name == this?.uppercase() }
        ?: CommunityThreadRole.MEMBER

private fun String.toChatMemberState() =
    CommunityThreadMemberState.entries.firstOrNull { it.name == uppercase() }
        ?: CommunityThreadMemberState.ACTIVE
