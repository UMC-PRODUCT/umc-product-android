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
        while (true) {
            when (val result = fetch(before)) {
                is ApiState.Fail -> return result
                is ApiState.Success -> {
                    val page = result.data
                    page.messages.forEach { messages.putIfAbsent(it.messageId, it) }
                    if (!page.hasMore || (oldestDisplayedId != null && oldestDisplayedId in messages)) {
                        return ApiState.Success(page.copy(messages = messages.values.toList()))
                    }
                    val next = page.nextBefore
                    if (next.isNullOrBlank() || !cursors.add(next)) {
                        return ApiState.Fail(FailState(code = "RECOVERY_INCOMPLETE", message = "대화 복구를 완료하지 못했어요. 다시 연결해주세요."))
                    }
                    before = next
                }
            }
        }
    }
}
