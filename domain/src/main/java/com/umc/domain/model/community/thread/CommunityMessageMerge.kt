package com.umc.domain.model.community.thread

import java.time.Instant

/** Deleted messages cannot be restored by a delayed edit or snapshot. */
object CommunityMessageMerge {
    fun merge(current: CommunityThreadMessage?, incoming: CommunityThreadMessage): CommunityThreadMessage {
        if (current == null) return incoming
        if (current.deletedAt != null || current.status == "DELETED") return current
        if (incoming.deletedAt != null || incoming.status == "DELETED") return incoming
        val currentTime = runCatching { Instant.parse(current.editedAt ?: current.createdAt) }.getOrNull()
        val incomingTime = runCatching { Instant.parse(incoming.editedAt ?: incoming.createdAt) }.getOrNull()
        return if (currentTime != null && incomingTime != null && incomingTime < currentTime) current else incoming
    }
}
