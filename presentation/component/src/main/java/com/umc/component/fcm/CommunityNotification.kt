package com.umc.component.fcm

import android.net.Uri

/** Shared by data notifications and the launcher extras of background mixed notifications. */
data class CommunityNotification(val threadId: String, val eventId: String?) {
    fun uri(): Uri = Uri.parse("https://api.university.neordinary.com/community/threads")
        .buildUpon().appendQueryParameter("threadId", threadId).build()

    companion object {
        fun parse(data: Map<String, String>): CommunityNotification? {
            if (data["schemaVersion"]?.let { it != "1" } == true) return null
            val threadId = data["threadId"]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return CommunityNotification(threadId, data["notificationEventId"]?.takeIf { it.isNotBlank() })
        }
    }
}

/** Only a RESUMED chat suppresses a local notification. Cleared when the app leaves it. */
object ActiveCommunityThread {
    @Volatile var threadId: String? = null
}
