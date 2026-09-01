package com.umc.component

import com.umc.component.base.SessionExpiryBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 세션 만료 신호는 구독자가 아직 붙기 전에 발생해도 유실되면 안 된다.
 * (만료는 어느 화면에서든 터질 수 있고, 그 순간 NavHost가 재구성 중일 수 있다)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionExpiryBusTest {

    @Test
    fun expirySignalRaisedBeforeSubscriptionIsStillDelivered() = runTest {
        // 구독자가 없는 상태에서 만료 발생
        SessionExpiryBus.notifyExpired()

        // NavHost가 뒤늦게 관찰을 시작
        val got = withTimeoutOrNull(500) { SessionExpiryBus.expired.first() }

        assertNotNull("구독 전에 발생한 세션 만료 신호가 유실됐다", got)
    }
}
