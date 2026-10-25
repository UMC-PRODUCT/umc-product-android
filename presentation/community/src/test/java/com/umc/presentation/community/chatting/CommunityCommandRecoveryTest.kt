package com.umc.presentation.community.chatting

import org.junit.Assert.*
import org.junit.Test

class CommunityCommandRecoveryTest {
    @Test fun `change before ack needs no recovery`() {
        val recovery = CommunityCommandRecovery()
        recovery.expect("command", "created:client")
        recovery.observed("created:client")
        assertFalse(recovery.acknowledge("command"))
        assertFalse(recovery.takeMissing())
    }

    @Test fun `change after ack cancels fallback`() {
        val recovery = CommunityCommandRecovery()
        recovery.expect("command", "reaction:message")
        assertTrue(recovery.acknowledge("command"))
        recovery.observed("reaction:message")
        assertFalse(recovery.takeMissing())
    }

    @Test fun `missing change requires only one fallback across multiple acks`() {
        val recovery = CommunityCommandRecovery()
        recovery.expect("a", "updated:1")
        recovery.expect("b", "deleted:2")
        assertTrue(recovery.acknowledge("a"))
        assertFalse(recovery.acknowledge("a"))
        recovery.acknowledge("b")
        recovery.observed("updated:unrelated")
        assertTrue(recovery.takeMissing())
        assertFalse(recovery.takeMissing())
    }

    @Test fun `failed commands and disconnected sessions have no fallback`() {
        val recovery = CommunityCommandRecovery()
        recovery.expect("a", "created:1")
        recovery.acknowledge("a")
        recovery.forget("a")
        assertFalse(recovery.takeMissing())
        recovery.expect("b", "created:2")
        recovery.acknowledge("b")
        recovery.clear()
        assertFalse(recovery.takeMissing())
    }
}
