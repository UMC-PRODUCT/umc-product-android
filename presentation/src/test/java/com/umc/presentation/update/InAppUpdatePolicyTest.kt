package com.umc.presentation.update

import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.umc.presentation.update.InAppUpdatePolicy.Action
import org.junit.Assert.assertEquals
import org.junit.Test

class InAppUpdatePolicyTest {
    private val available = UpdateAvailability.UPDATE_AVAILABLE
    private val inProgress = UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

    private fun decide(
        availability: Int = available,
        installStatus: Int = InstallStatus.UNKNOWN,
        priority: Int = 0,
        immediateAllowed: Boolean = true,
        flexibleAllowed: Boolean = true,
        immediateStarted: Boolean = false,
        immediateFailed: Boolean = false,
        flexibleAsked: Boolean = false,
    ) = InAppUpdatePolicy.decide(
        availability, installStatus, priority, immediateAllowed, flexibleAllowed,
        immediateStarted, immediateFailed, flexibleAsked,
    )

    @Test fun `no update shows nothing`() {
        assertEquals(Action.NONE, decide(availability = UpdateAvailability.UPDATE_NOT_AVAILABLE))
        assertEquals(Action.NONE, decide(availability = UpdateAvailability.UNKNOWN))
    }

    @Test fun `normal update asks flexible once per run`() {
        assertEquals(Action.FLEXIBLE, decide(priority = 0))
        assertEquals(Action.FLEXIBLE, decide(priority = InAppUpdatePolicy.IMMEDIATE_PRIORITY - 1))
        assertEquals(Action.NONE, decide(flexibleAsked = true))
    }

    @Test fun `high priority update is immediate on every resume`() {
        assertEquals(Action.IMMEDIATE, decide(priority = InAppUpdatePolicy.IMMEDIATE_PRIORITY))
        // 유연한 업데이트를 이미 물었어도 긴급 업데이트는 다시 띄운다
        assertEquals(Action.IMMEDIATE, decide(priority = 5, flexibleAsked = true))
    }

    @Test fun `falls back when immediate is not allowed or failed`() {
        assertEquals(Action.FLEXIBLE, decide(priority = 5, immediateAllowed = false))
        assertEquals(Action.FLEXIBLE, decide(priority = 5, immediateFailed = true))
        assertEquals(Action.NONE, decide(priority = 0, flexibleAllowed = false))
    }

    @Test fun `in progress immediate update started by the app is resumed regardless of priority`() {
        // 진행 중 상태의 우선순위 값은 정의되지 않아 0 으로 올 수 있다
        assertEquals(Action.IMMEDIATE, decide(inProgress, InstallStatus.DOWNLOADING, priority = 0, immediateStarted = true))
        assertEquals(Action.NONE, decide(inProgress, InstallStatus.DOWNLOADING, priority = 0, immediateStarted = true, immediateFailed = true))
    }

    @Test fun `in progress flexible download is not turned into an immediate update`() {
        assertEquals(Action.NONE, decide(inProgress, InstallStatus.DOWNLOADING, priority = 5, immediateStarted = false))
    }

    @Test fun `downloaded flexible update asks to restart`() {
        assertEquals(Action.PROMPT_INSTALL, decide(inProgress, InstallStatus.DOWNLOADED))
        assertEquals(Action.PROMPT_INSTALL, decide(inProgress, InstallStatus.DOWNLOADED, flexibleAsked = true))
    }

    @Test fun `downloaded immediate update is resumed instead of a postponable prompt`() {
        assertEquals(Action.IMMEDIATE, decide(inProgress, InstallStatus.DOWNLOADED, immediateStarted = true))
        assertEquals(
            Action.PROMPT_INSTALL,
            decide(inProgress, InstallStatus.DOWNLOADED, immediateStarted = true, immediateFailed = true),
        )
    }
}
