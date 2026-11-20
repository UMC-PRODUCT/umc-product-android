package com.umc.domain.model.community.thread

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** 끊긴 사이의 메시지를 다시 받아 오는 CommunityMessageRecovery 의 속성. */
class CommunityMessageRecoveryPropertyTest {

    /** 최신 → 오래된 순서의 대화 이력을 페이지로 나눠 주는 서버. inclusive 면 다음 페이지가 경계 메시지를 다시 준다. */
    private class Server(size: Int, val limit: Int, val inclusive: Boolean, val failAt: Int? = null) {
        val history = (size downTo 1).map { CommunityThreadMessage(messageId = "m$it", content = "message $it") }
        val failure = ApiState.Fail(FailState(code = "NETWORK_ERROR", message = "offline"))
        var calls = 0

        fun fetch(before: String?): ApiState<CommunityThreadMessagePage> {
            calls++
            check(calls <= history.size + 5) { "요청이 끝나지 않습니다(${calls}회)" }
            if (calls == failAt) return failure
            val start = if (before == null) 0 else history.indexOfFirst { it.messageId == before } + if (inclusive) 0 else 1
            val end = (start + limit).coerceAtMost(history.size)
            val page = history.subList(start, end)
            return ApiState.Success(CommunityThreadMessagePage(page, end < history.size, page.lastOrNull()?.messageId))
        }
    }

    /** 앵커: 없음(-1), 이력 안의 메시지(0..), 서버에서 사라진 메시지(-2). */
    private fun anchorOf(server: Server, pick: Int): String? = when {
        pick == -1 -> null
        pick == -2 -> "gone"
        else -> server.history[pick % server.history.size].messageId
    }

    /** 앵커가 들어 있는 페이지 끝까지가 기대 결과다. 앵커가 없거나 사라졌으면 이력 전체. */
    private fun expectedCount(server: Server, anchor: String?): Int {
        val index = server.history.indexOfFirst { it.messageId == anchor }
        if (index < 0) return server.history.size
        var start = 0
        while (true) {
            val end = (start + server.limit).coerceAtMost(server.history.size)
            if (index < end) return end
            start = if (server.inclusive) end - 1 else end
        }
    }

    @Test
    fun `정상 서버에서는 앵커가 든 페이지까지 빠짐없이 중복 없이 받는다`() = runBlocking<Unit> {
        checkAll(300, Arb.int(1..60), Arb.int(2..10), Arb.boolean(), Arb.int(-2..59)) { size, limit, inclusive, pick ->
            val server = Server(size, limit, inclusive)
            val anchor = anchorOf(server, pick)
            val result = CommunityMessageRecovery.recover(anchor) { server.fetch(it) }
            check(result is ApiState.Success) { "실패: $result" }
            val k = expectedCount(server, anchor)
            assertEquals(server.history.take(k).map { it.messageId }, result.data.messages.map { it.messageId })
            assertEquals(k < size, result.data.hasMore)
            val maxCalls = if (inclusive) (size - 1 + limit - 2) / (limit - 1) + 1 else (size + limit - 1) / limit
            assertTrue("요청 ${server.calls}회 > $maxCalls", server.calls <= maxCalls.coerceAtLeast(1))
        }
    }

    @Test
    fun `중간에 실패하면 받은 페이지를 버리고 그 실패를 그대로 돌려준다`() = runBlocking<Unit> {
        checkAll(300, Arb.int(1..60), Arb.int(2..10), Arb.boolean(), Arb.int(1..6)) { size, limit, inclusive, failAt ->
            val reference = Server(size, limit, inclusive).also { s -> CommunityMessageRecovery.recover(null) { s.fetch(it) } }
            val server = Server(size, limit, inclusive, failAt)
            val result = CommunityMessageRecovery.recover(null) { server.fetch(it) }
            if (failAt <= reference.calls) {
                assertSame(server.failure, result)
                assertEquals(failAt, server.calls)
            } else {
                assertTrue(result is ApiState.Success)
            }
        }
    }

    @Test
    fun `새 메시지 없이 커서만 바꾸는 서버에서도 복구가 끝난다`() = runBlocking<Unit> {
        checkAll(300, Arb.int(0..3), Arb.boolean()) { firstPageSize, repeatSeen ->
            var calls = 0
            val seen = (1..firstPageSize).map { CommunityThreadMessage(messageId = "m$it") }
            val result = CommunityMessageRecovery.recover("never-arrives") { before ->
                calls++
                check(calls <= 50) { "요청이 끝나지 않습니다(${calls}회)" }
                // 첫 페이지 뒤로는 빈 페이지나 이미 받은 메시지만 주면서 매번 새 커서와 hasMore=true 를 돌려준다.
                val messages = if (before == null) seen else if (repeatSeen) seen else emptyList()
                ApiState.Success(CommunityThreadMessagePage(messages, true, "cursor-$calls"))
            }
            assertTrue("결과 $result", result is ApiState.Fail && result.failState.code == "RECOVERY_INCOMPLETE")
        }
    }

    @Test
    fun `앵커가 든 페이지라도 이어 받을 커서가 없으면 성공으로 끝내지 않는다`() = runBlocking<Unit> {
        val page = listOf(CommunityThreadMessage(messageId = "anchor"))
        listOf(null, "", " ").forEach { cursor ->
            val pending = CommunityMessageRecovery.recover("anchor") {
                ApiState.Success(CommunityThreadMessagePage(page, true, cursor))
            }
            assertTrue("hasMore=true, 커서 '$cursor' → $pending", pending is ApiState.Fail && pending.failState.code == "RECOVERY_INCOMPLETE")
            // 마지막 페이지의 빈 커서는 정상이다.
            val last = CommunityMessageRecovery.recover("anchor") {
                ApiState.Success(CommunityThreadMessagePage(page, false, cursor))
            }
            assertTrue("hasMore=false, 커서 '$cursor' → $last", last is ApiState.Success)
        }
    }
}
