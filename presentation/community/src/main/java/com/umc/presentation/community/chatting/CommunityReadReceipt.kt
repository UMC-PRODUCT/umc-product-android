package com.umc.presentation.community.chatting

/** A queued command is pending until the server acknowledges it. IDs are ordered newest first. */
internal class CommunityReadReceipt {
    var observed: String? = null
    private var confirmed: String? = null
    var pending: Pair<String, String>? = null
        private set

    fun candidate(ids: List<String>): String? {
        if (pending != null) return null
        val target = observed ?: return null
        val index = ids.indexOf(target)
        val confirmedIndex = ids.indexOf(confirmed)
        return target.takeIf { index >= 0 && target != confirmed && (confirmedIndex < 0 || index < confirmedIndex) }
    }

    fun queued(commandId: String, messageId: String) { pending = commandId to messageId }

    fun failed(commandId: String): Boolean {
        if (pending?.first != commandId) return false
        pending = null
        return true
    }

    fun acknowledged(commandId: String, ids: List<String>): Boolean {
        val messageId = pending?.takeIf { it.first == commandId }?.second ?: return false
        pending = null
        confirm(messageId, ids)
        return true
    }

    fun confirm(messageId: String, ids: List<String>) {
        val incoming = ids.indexOf(messageId)
        val previous = ids.indexOf(confirmed)
        if (incoming >= 0 && (previous < 0 || incoming < previous)) confirmed = messageId
        val pendingIndex = ids.indexOf(pending?.second)
        if (incoming >= 0 && pendingIndex >= incoming) pending = null
    }
}
