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
                    // 이어 받을 페이지가 있다면서 커서가 없으면, 호출자가 빈 커서로 이전 메시지를 요청해 최신 페이지를 다시 받는다.
                    // 앵커를 찾았더라도 성공으로 끝내지 않는다. 마지막 페이지(hasMore=false)의 빈 커서는 그대로 허용한다.
                    if (page.hasMore && page.nextBefore.isNullOrBlank()) return incomplete()
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

    /** 새 메시지가 없는 페이지가 연달아 이 수에 이르면 멈춘다. 그보다 짧은 빈 구간(MAX_IDLE_PAGES - 1 페이지까지)은 넘어간다. */
    private const val MAX_IDLE_PAGES = 3
}
