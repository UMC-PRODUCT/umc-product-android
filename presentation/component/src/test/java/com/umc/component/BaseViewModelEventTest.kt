package com.umc.component

import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * 단발성 이벤트 전달 보장 테스트.
 *
 * 화면 회전·다크모드 전환 등으로 Activity가 재생성되면 컴포지션이 파괴되어
 * 구독자 수가 잠시 0이 된다. 그 사이 ViewModel(살아 있음)이 내보낸 이벤트가
 * 사라지지 않아야 한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelEventTest {

    private data class TestState(val v: Int = 0) : UiState
    private sealed interface TestEvent : UiEvent {
        data class Ping(val n: Int) : TestEvent
    }

    private class TestViewModel : BaseViewModel<TestState, TestEvent>(TestState()) {
        fun fire(n: Int) = emitEvent(TestEvent.Ping(n))
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    /** 구독자가 0인 순간에 발행된 이벤트도 이후 구독자에게 전달되어야 한다. */
    @Test
    fun eventEmittedWithoutSubscriberIsStillDelivered() = runTest {
        val vm = TestViewModel()

        // 구독자가 아직 없는 상태(= 화면 재생성 구간)에서 발행
        vm.fire(1)

        // 새 컴포지션이 붙어 구독을 시작
        val received = withTimeoutOrNull(500) { vm.uiEvent.first() }

        assertNotNull("구독자 0인 구간에 발행된 이벤트가 유실됐다", received)
        assertEquals(TestEvent.Ping(1), received)
    }

    /** 재생성 구간에 쌓인 이벤트는 순서대로 정확히 한 번씩 전달되어야 한다. */
    @Test
    fun bufferedEventsAreDeliveredInOrderExactlyOnce() = runTest {
        val vm = TestViewModel()

        vm.fire(1)
        vm.fire(2)
        vm.fire(3)

        val got = mutableListOf<TestEvent>()
        val job = launch { vm.uiEvent.toList(got) }
        withTimeoutOrNull(500) {
            while (got.size < 3) kotlinx.coroutines.yield()
        }
        job.cancel()

        assertEquals(listOf(TestEvent.Ping(1), TestEvent.Ping(2), TestEvent.Ping(3)), got)
    }
}
