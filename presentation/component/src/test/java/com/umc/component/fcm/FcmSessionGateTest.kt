package com.umc.component.fcm

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FcmSessionGateTest {
    @Test fun `진행 중인 등록이 끝난 뒤 해제하며 대기하던 이전 등록은 무시한다`() = runTest {
        val gate = FcmSessionGate()
        val oldGeneration = gate.generation
        val releaseRegistration = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        launch {
            gate.runIfCurrent(oldGeneration) {
                events += "등록 시작"
                releaseRegistration.await()
                events += "등록 완료"
            }
        }
        runCurrent()
        launch { gate.runIfCurrent(oldGeneration) { events += "지연 등록" } }
        launch { gate.logout { events += "등록 해제"; events += "세션 삭제" } }
        runCurrent()
        assertEquals(listOf("등록 시작"), events)
        releaseRegistration.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf("등록 시작", "등록 완료", "등록 해제", "세션 삭제"), events)
    }

    @Test fun `로그아웃 이전 콜백은 버리고 재로그인 세대 작업은 허용한다`() = runTest {
        val gate = FcmSessionGate()
        val previous = gate.generation
        gate.logout { }
        var oldCallbackRan = false
        var newCallbackRan = false
        gate.runIfCurrent(previous) { oldCallbackRan = true }
        gate.runIfCurrent(gate.generation) { newCallbackRan = true }
        assertTrue(!oldCallbackRan)
        assertTrue(newCallbackRan)
    }

    @Test fun `등록 실패 후에도 로그아웃 정리는 실행할 수 있다`() = runTest {
        val gate = FcmSessionGate()
        runCatching { gate.runIfCurrent(gate.generation) { error("등록 실패") } }
        var cleaned = false
        gate.logout { cleaned = true }
        assertTrue(cleaned)
    }
}
