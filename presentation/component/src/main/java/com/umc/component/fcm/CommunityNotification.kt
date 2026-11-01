package com.umc.component.fcm

import android.net.Uri

/** Shared by data notifications and the launcher extras of background mixed notifications. */
data class CommunityNotification(val threadId: String, val eventId: String?) {
    fun uri(): Uri = Uri.parse("umc://thread")
        .buildUpon().appendQueryParameter("threadId", threadId).build()

    companion object {
        fun parse(data: Map<String, String>): CommunityNotification? {
            if (data["schemaVersion"]?.let { it != "1" } == true) return null
            val threadId = data["threadId"]?.trim()?.takeIf { it.isNotEmpty() }
                ?: sequenceOf(data["deepLink"], data["deeplink"])
                    .filterNotNull().mapNotNull { threadIdFromLink(it) }.firstOrNull()
                ?: return null
            return CommunityNotification(threadId, data["notificationEventId"]?.takeIf { it.isNotBlank() })
        }

        private fun threadIdFromLink(link: String): String? = runCatching {
            val uri = Uri.parse(link)
            // 서버의 스레드 알림은 umc://thread/{threadId} 형식으로 전달된다.
            if (uri.scheme == "umc" && uri.host == "thread" && uri.pathSegments.size == 1) {
                return@runCatching uri.pathSegments.single().trim().takeIf { it.isNotEmpty() }
            }
            val supported = (uri.scheme == "umc" && uri.host == "thread" && uri.path.isNullOrEmpty()) ||
                (uri.scheme == "https" && uri.host == "api.university.neordinary.com" &&
                    uri.path == "/community/threads")
            if (supported) uri.getQueryParameter("threadId")?.trim()?.takeIf { it.isNotEmpty() }
            else null
        }.getOrNull()
    }
}

/** Only a RESUMED chat suppresses a local notification. Cleared when the app leaves it. */
object ActiveCommunityThread {
    @Volatile var threadId: String? = null
}
