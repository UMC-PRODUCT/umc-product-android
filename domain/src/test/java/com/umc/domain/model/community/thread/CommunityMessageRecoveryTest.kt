package com.umc.domain.model.community.thread

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CommunityMessageRecoveryTest {
    private fun message(id: Int) = CommunityThreadMessage(messageId = id.toString(), content = "message $id")

    @Test fun `recovers 75 missed messages and older displayed edits deletions and reactions`() = runBlocking {
        val server = (100 downTo 1).map(::message).map {
            when (it.messageId) {
                "20" -> it.copy(content = "edited", editedAt = "now")
                "10" -> it.copy(content = null, deletedAt = "now")
                "5" -> it.copy(reactions = listOf(CommunityReaction(emoji = "ok", count = "1")))
                else -> it
            }
        }
        val requested = mutableListOf<String?>()
        val result = CommunityMessageRecovery.recover("1") { cursor ->
            requested += cursor
            val offset = cursor?.toInt() ?: 0
            val end = (offset + 30).coerceAtMost(server.size)
            ApiState.Success(CommunityThreadMessagePage(server.subList(offset, end), end < server.size, end.toString()))
        } as ApiState.Success
        assertEquals(4, requested.size)
        assertEquals(100, result.data.messages.size)
        assertEquals(100, result.data.messages.distinctBy { it.messageId }.size)
        assertEquals("edited", result.data.messages.first { it.messageId == "20" }.content)
        assertNotNull(result.data.messages.first { it.messageId == "10" }.deletedAt)
        assertEquals("1", result.data.messages.first { it.messageId == "5" }.reactions.single().count)
    }

    @Test fun `empty initial room still recovers every missed page`() = runBlocking {
        var calls = 0
        val result = CommunityMessageRecovery.recover(null) { cursor ->
            calls++
            if (cursor == null) ApiState.Success(CommunityThreadMessagePage(listOf(message(2)), true, "next"))
            else ApiState.Success(CommunityThreadMessagePage(listOf(message(1)), false))
        } as ApiState.Success
        assertEquals(2, calls)
        assertEquals(listOf("2", "1"), result.data.messages.map { it.messageId })
    }

    @Test fun `repeated cursor leaves recovery incomplete`() = runBlocking {
        val result = CommunityMessageRecovery.recover("oldest") {
            ApiState.Success(CommunityThreadMessagePage(listOf(message(1)), true, "same"))
        }
        assertTrue(result is ApiState.Fail)
    }

    @Test fun `intermediate failure never returns partial messages`() = runBlocking {
        val failure = ApiState.Fail(FailState(code = "NETWORK_ERROR", message = "offline"))
        val result = CommunityMessageRecovery.recover("oldest") { cursor ->
            if (cursor == null) ApiState.Success(CommunityThreadMessagePage(listOf(message(1)), true, "next"))
            else failure
        }
        assertEquals(failure, result)
    }

    @Test fun `anchor page preserves continuation for older history and removes overlap`() = runBlocking {
        val result = CommunityMessageRecovery.recover("2") { cursor ->
            if (cursor == null) ApiState.Success(CommunityThreadMessagePage(listOf(message(4), message(3)), true, "3"))
            else ApiState.Success(CommunityThreadMessagePage(listOf(message(3), message(2)), true, "2"))
        } as ApiState.Success
        assertEquals(listOf("4", "3", "2"), result.data.messages.map { it.messageId })
        assertTrue(result.data.hasMore)
        assertEquals("2", result.data.nextBefore)
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is not reported as successful recovery`() { runBlocking {
        CommunityMessageRecovery.recover("oldest") { throw CancellationException("leave room") }
    } }
}
