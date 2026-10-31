package com.umc.component.fcm

import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 등록·알림 표시와 로그아웃을 직렬화하고, 이전 세션에 예약된 작업을 무효화한다. */
internal class FcmSessionGate {
    private val mutex = Mutex()
    private val revision = AtomicLong()
    val generation: Long get() = revision.get()

    suspend fun runIfCurrent(expected: Long, action: suspend () -> Unit) {
        mutex.withLock {
            if (expected == generation) action()
        }
    }

    suspend fun logout(action: suspend () -> Unit) {
        revision.incrementAndGet()
        mutex.withLock { action() }
    }
}
