package com.umc.component.fcm

import org.junit.Assert.*
import org.junit.Test

class CommunityNotificationTest {
    @Test fun `mixed payload keeps one event and target room`() {
        val route = CommunityNotification.parse(mapOf(
            "schemaVersion" to "1", "type" to "thread.message", "notificationEventId" to "event-1",
            "threadId" to "42", "title" to "title", "body" to "body",
        ))
        assertEquals(CommunityNotification("42", "event-1"), route)
    }

    @Test fun `missing target and unsupported schema cannot open a room`() {
        assertNull(CommunityNotification.parse(mapOf("title" to "hello")))
        assertNull(CommunityNotification.parse(mapOf("threadId" to " ")))
        assertNull(CommunityNotification.parse(mapOf("schemaVersion" to "2", "threadId" to "42")))
    }

    @Test fun `legacy thread payload remains compatible`() {
        assertEquals(CommunityNotification("42", null), CommunityNotification.parse(mapOf("threadId" to "42")))
    }
}
