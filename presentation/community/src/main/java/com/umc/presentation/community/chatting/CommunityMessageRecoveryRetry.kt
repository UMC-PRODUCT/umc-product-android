package com.umc.presentation.community.chatting

import kotlinx.coroutines.delay

// A connected socket does not guarantee that HTTP recovery is available yet.
internal suspend fun recoverMessagesWithRetry(
    wait: suspend (Long) -> Unit = { delay(it) },
    recover: suspend () -> Boolean,
): Boolean {
    var retryDelay = 1_000L
    repeat(3) { attempt ->
        if (recover()) return true
        if (attempt < 2) {
            wait(retryDelay)
            retryDelay *= 2
        }
    }
    return false
}
