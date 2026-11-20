package com.umc.domain.model.community.thread

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState

/** Reads through the oldest displayed message so edits, deletions and reactions are refreshed too.
 * History pagination belongs to the caller; a failed or looping recovery never commits a partial page.
 */
object CommunityMessageRecovery {
    suspend fun recover(
        oldestDisplayedId: String?,
        fetch: suspend (String?) -> ApiState<CommunityThreadMessagePage>,
    ): ApiState<CommunityThreadMessagePage> {
        val messages = linkedMapOf<String, CommunityThreadMessage>()
        val cursors = hashSetOf<String>()
        var before: String? = null
        var idlePages = 0
        while (true) {
            when (val result = fetch(before)) {
                is ApiState.Fail -> return result
                is ApiState.Success -> {
                    val page = result.data
                    val known = messages.size
                    page.messages.forEach { messages.putIfAbsent(it.messageId, it) }
                    if (!page.hasMore || (oldestDisplayedId != null && oldestDisplayedId in messages)) {
                        return ApiState.Success(page.copy(messages = messages.values.toList()))
                    }
                    // 새 메시지 없이 커서만 바뀌는 페이지가 이어지면 이력이 줄지 않으므로 멈춘다.
                    // 멈추지 않으면 요청이 끝없이 이어지고, 그동안 이전 메시지 불러오기도 같은 잠금에 막힌다.
                    idlePages = if (messages.size > known) 0 else idlePages + 1
                    val next = page.nextBefore
                    if (next.isNullOrBlank() || !cursors.add(next) || idlePages >= MAX_IDLE_PAGES) return incomplete()
                    before = next
                }
            }
        }
    }

    private fun incomplete() =
        ApiState.Fail(FailState(code = "RECOVERY_INCOMPLETE", message = "대화 복구를 완료하지 못했어요. 다시 연결해주세요."))

    /** 새 메시지가 없는 페이지를 연달아 몇 번까지 견딜지. 서버가 걸러 낸 빈 구간 정도는 넘어간다. */
    private const val MAX_IDLE_PAGES = 3
}
