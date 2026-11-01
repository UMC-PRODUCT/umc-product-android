package com.umc.component.fcm

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** 오래된 StringSet은 순서를 복구할 수 없으므로 결정적인 순서로 한 번 이관한다. */
object NotificationEventHistory {
    fun decode(encoded: String?, legacy: Set<String>): List<String> =
        (encoded?.let { runCatching { Json.decodeFromString<List<String>>(it) }.getOrNull() }
            ?: legacy.sorted()).distinct().takeLast(200)

    fun encode(history: List<String>): String = Json.encodeToString(history)

    fun record(history: List<String>, eventId: String): List<String> =
        (history.filterNot { it == eventId } + eventId).takeLast(200)
}
