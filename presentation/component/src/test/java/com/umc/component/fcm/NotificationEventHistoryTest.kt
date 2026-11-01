package com.umc.component.fcm

import org.junit.Assert.*
import org.junit.Test

class NotificationEventHistoryTest {
    @Test fun newest200EventIdsKeepTheirInsertionOrderAcrossPersistence() {
        var history = emptyList<String>()
        repeat(205) { history = NotificationEventHistory.record(history, "event-$it") }
        assertEquals((5..204).map { "event-$it" }, history)
        assertEquals(history, NotificationEventHistory.decode(NotificationEventHistory.encode(history), emptySet()))
    }

    @Test fun migrationKeepsLegacyMembershipAndNewHistoryTakesPrecedence() {
        val legacy = setOf("event-b", "event-a")
        assertEquals(listOf("event-a", "event-b"), NotificationEventHistory.decode(null, legacy))
        val ordered = listOf("event-b", "event-a")
        assertEquals(ordered, NotificationEventHistory.decode(NotificationEventHistory.encode(ordered), setOf("old")))
        assertEquals(listOf("event-a", "event-b"), NotificationEventHistory.decode("broken", legacy))
    }

    @Test fun duplicateRecordingNeverConsumesTwoHistorySlots() {
        assertEquals(listOf("b", "a"), NotificationEventHistory.record(listOf("a", "b"), "a"))
    }
}
