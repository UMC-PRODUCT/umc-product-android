package com.umc.presentation.community.chatting

/** Tracks commands whose corresponding realtime change has not arrived yet. */
internal class CommunityCommandRecovery {
    private val expected = mutableMapOf<String, String>()
    private val acknowledged = mutableSetOf<String>()

    fun expect(commandId: String, change: String) { expected[commandId] = change }
    fun observed(change: String) {
        val delivered = expected.filterValues { it == change }.keys
        delivered.forEach { expected.remove(it); acknowledged.remove(it) }
    }
    fun acknowledge(commandId: String): Boolean =
        commandId in expected && acknowledged.add(commandId)

    fun takeMissing(): Boolean {
        val missing = acknowledged.any { it in expected }
        acknowledged.forEach(expected::remove)
        acknowledged.clear()
        return missing
    }
    val hasAcknowledged: Boolean get() = acknowledged.isNotEmpty()
    fun forget(commandId: String) { expected.remove(commandId); acknowledged.remove(commandId) }
    fun clear() { expected.clear(); acknowledged.clear() }
}
