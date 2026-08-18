package com.umc.data.mapper.community

import com.umc.data.response.community.CommunityThreadDetailResponse
import com.umc.data.response.community.CommunityThreadLastMessageResponse
import com.umc.data.response.community.CommunityThreadListResponse
import com.umc.data.response.community.CommunityThreadSummaryResponse
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadDetail
import com.umc.domain.model.community.CommunityThreadLastMessage
import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.model.community.CommunityThreadRole

fun CommunityThreadListResponse.toDomain(): CommunityThreadPage {
    return CommunityThreadPage(
        pinnedThreads = pinned.map { response ->
            response.toDomain()
        },
        threads = threads.map { response ->
            response.toDomain()
        },
        nextOffset = nextOffset,
        total = total.toIntOrNull() ?: 0,
    )
}

fun CommunityThreadSummaryResponse.toDomain(): CommunityThread {
    return CommunityThread(
        threadId = threadId,
        title = title,
        description = description.orEmpty(),
        category = category.toThreadCategory(),
        icon = icon,
        memberCount = memberCount.toIntOrNull() ?: 0,
        unreadCount = unreadCount.toIntOrNull() ?: 0,
        maxMembers = maxMembers.toIntOrNull() ?: 0,
        isPinned = isPinned,
        isMuted = isMuted,
        myRole = myRole.toThreadRole(),
        lastMessage = lastMessage?.toDomain(),
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isJoined = isJoined,
    )
}

fun CommunityThreadDetailResponse.toDomain(): CommunityThreadDetail {
    return CommunityThreadDetail(
        threadId = threadId,
        title = title,
        description = description.orEmpty(),
        category = category.toThreadCategory(),
        icon = icon,
        memberCount = memberCount.toIntOrNull() ?: 0,
        unreadCount = unreadCount.toIntOrNull() ?: 0,
        maxMembers = maxMembers.toIntOrNull() ?: 0,
        isPinned = isPinned,
        isMuted = isMuted,
        myRole = myRole.toThreadRole(),
        lastMessage = lastMessage?.toDomain(),
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
        shareUrl = shareUrl,
        deletedAt = deletedAt,
        isJoined = isJoined,
    )
}

private fun CommunityThreadLastMessageResponse.toDomain(): CommunityThreadLastMessage {
    return CommunityThreadLastMessage(
        preview = preview,
        senderName = senderName,
        createdAt = createdAt,
    )
}

private fun String.toThreadCategory(): CommunityThreadCategory {
    return when (uppercase()) {
        "STUDY" -> CommunityThreadCategory.STUDY
        "QNA" -> CommunityThreadCategory.QNA
        "PROJECT" -> CommunityThreadCategory.PROJECT
        "FREE" -> CommunityThreadCategory.FREE
        else -> CommunityThreadCategory.UNKNOWN
    }
}

private fun String?.toThreadRole(): CommunityThreadRole {
    return when (this?.uppercase()) {
        "OWNER" -> CommunityThreadRole.OWNER
        "ADMIN" -> CommunityThreadRole.ADMIN
        "MEMBER" -> CommunityThreadRole.MEMBER
        else -> CommunityThreadRole.UNKNOWN
    }
}
