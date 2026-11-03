package com.umc.component.fcm

import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext

/** 등록·알림 표시와 로그아웃을 직렬화하고, 이전 세션에 예약된 작업을 무효화한다. */
internal class FcmSessionGate {
    private val mutex = Mutex()
    private val revision = AtomicLong()
    private val jobLock = Any()
    private val registrationJobs = mutableSetOf<Job>()
    private var logoutsInProgress = 0
    val generation: Long get() = revision.get()

    suspend fun runIfCurrent(expected: Long, action: suspend () -> Unit) {
        val job = requireNotNull(currentCoroutineContext()[Job])
        synchronized(jobLock) {
            if (expected != generation || logoutsInProgress > 0) return
            registrationJobs += job
        }
        try {
            mutex.withLock {
                if (expected == generation) action()
            }
        } finally {
            synchronized(jobLock) { registrationJobs -= job }
        }
    }

    suspend fun logout(action: suspend () -> Unit) {
        val pending = synchronized(jobLock) {
            revision.incrementAndGet()
            logoutsInProgress++
            registrationJobs.toList()
        }
        pending.forEach { it.cancel() }
        try {
            mutex.withLock { action() }
        } finally {
            synchronized(jobLock) { logoutsInProgress-- }
        }
    }
}
