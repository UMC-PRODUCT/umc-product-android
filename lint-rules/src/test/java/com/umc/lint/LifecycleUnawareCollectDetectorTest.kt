package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class LifecycleUnawareCollectDetectorTest {

    @Test
    fun `LaunchedEffect 안에서 바로 collect하면 오류`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import kotlinx.coroutines.flow.Flow

                fun HomeRoute(events: Flow<String>) {
                    LaunchedEffect(Unit) {
                        events.collect { }
                    }
                }
                """,
            ).indented(),
        ).expectErrorCount(1).expectContains("repeatOnLifecycle")
    }

    @Test
    fun `repeatOnLifecycle·flowWithLifecycle 수집과 snapshotFlow 수집은 허용`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.compose.runtime.snapshotFlow as observeScroll
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.Lifecycle
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow

                fun HomeRoute(events: Flow<String>, scrollOffset: () -> Int, lifecycle: Lifecycle) {
                    LaunchedEffect(Unit) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            events.collect { }
                        }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).collect { }
                    }
                    LaunchedEffect(Unit) {
                        observeScroll { scrollOffset() }.collect { }
                    }
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `LaunchedEffect 밖의 수집은 무시`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import kotlinx.coroutines.flow.Flow

                suspend fun observe(events: Flow<String>) {
                    events.collect { }
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `Compose가 아닌 동명 snapshotFlow는 예외로 보지 않는다`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import sample.snapshotFlow as routeEvents

                fun HomeRoute() {
                    LaunchedEffect(Unit) {
                        routeEvents().collect { }
                    }
                }
                """,
            ).indented(),
        ).expectErrorCount(1)
    }

    @Test
    fun `merge의 일부 Flow만 보호하면 오류, 합친 Flow 전체를 보호하면 허용`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.compose.runtime.snapshotFlow
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.Lifecycle
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.merge

                fun HomeRoute(events: Flow<String>, other: Flow<String>, lifecycle: Lifecycle) {
                    LaunchedEffect(Unit) {
                        merge(events, other.flowWithLifecycle(lifecycle)).collect { }
                    }
                    LaunchedEffect(Unit) {
                        merge(snapshotFlow { "scroll" }, events).collect { }
                    }
                    LaunchedEffect(Unit) {
                        merge(events, other).flowWithLifecycle(lifecycle).collect { }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:12: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    merge(events, other.flowWithLifecycle(lifecycle)).collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:15: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    merge(snapshotFlow { "scroll" }, events).collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `Compose가 아닌 동명 LaunchedEffect는 무시`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import kotlinx.coroutines.flow.Flow
                import sample.LaunchedEffect

                fun Poller(events: Flow<String>) {
                    LaunchedEffect(Unit) {
                        events.collect { }
                    }
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `보호 연산자 뒤에 다른 Flow를 합치면 오류, 값만 바꾸는 연산자는 허용`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.compose.runtime.snapshotFlow
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.Lifecycle
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.combine
                import kotlinx.coroutines.flow.distinctUntilChanged
                import kotlinx.coroutines.flow.flatMapLatest
                import kotlinx.coroutines.flow.map

                fun HomeRoute(events: Flow<String>, lifecycle: Lifecycle, offset: () -> Int) {
                    LaunchedEffect(Unit) {
                        snapshotFlow { offset() }.combine(events) { a, b -> a to b }.collect { }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).flatMapLatest { events }.collect { }
                    }
                    LaunchedEffect(Unit) {
                        snapshotFlow { offset() }.map { it * 2 }.distinctUntilChanged().collect { }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:15: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    snapshotFlow { offset() }.combine(events) { a, b -> a to b }.collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:18: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle).flatMapLatest { events }.collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `CREATED 로 내린 보호, 인수 계산 중 수집, 보호 뒤 onStart 방출은 오류`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.lifecycle.Lifecycle
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.onStart

                fun HomeRoute(events: Flow<String>, lifecycle: Lifecycle) {
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { }
                    }
                    LaunchedEffect(Unit) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) { events.collect { } }
                    }
                    LaunchedEffect(Unit) {
                        lifecycle.repeatOnLifecycle(run { events.collect { }; Lifecycle.State.STARTED }) { }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).onStart { emit("navigate") }.collect { }
                    }
                    LaunchedEffect(Unit) {
                        events.onStart { emit("navigate") }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).collect { }
                    }
                    LaunchedEffect(Unit) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { events.collect { } }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:12: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:15: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) { events.collect { } }
                                                                           ~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:18: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    lifecycle.repeatOnLifecycle(run { events.collect { }; Lifecycle.State.STARTED }) { }
                                                      ~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:21: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle).onStart { emit("navigate") }.collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
    }

    @Test
    fun `라벨 붙은 보호 블록과 바깥 보호가 있는 CREATED 게이트는 허용, 보호 뒤 retryWhen 방출은 오류`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.compose.runtime.snapshotFlow
                import androidx.lifecycle.Lifecycle
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.retryWhen

                fun HomeRoute(events: Flow<String>, lifecycle: Lifecycle, offset: () -> Int) {
                    LaunchedEffect(Unit) {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) events@{ events.collect { } }
                    }
                    LaunchedEffect(Unit) {
                        snapshotFlow { offset() }.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).retryWhen { _, _ -> emit("navigate"); false }.collect { }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:22: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle).retryWhen { _, _ -> emit("navigate"); false }.collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `지역 val 에 담은 보호와 상태, 보호 뒤 debounce 를 실제 동작대로 판정`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.lifecycle.Lifecycle
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.debounce

                fun HomeRoute(events: Flow<String>, lifecycle: Lifecycle) {
                    LaunchedEffect(Unit) {
                        val visibleEvents = events.flowWithLifecycle(lifecycle)
                        visibleEvents.collect { }
                    }
                    LaunchedEffect(Unit) {
                        val alias = events
                        alias.collect { }
                    }
                    LaunchedEffect(Unit) {
                        val minimum = Lifecycle.State.CREATED
                        events.flowWithLifecycle(lifecycle, minimum).collect { }
                    }
                    LaunchedEffect(Unit) {
                        val minimum = Lifecycle.State.CREATED
                        lifecycle.repeatOnLifecycle(minimum) { events.collect { } }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).debounce(300).collect { }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:17: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    alias.collect { }
                    ~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:21: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle, minimum).collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:25: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    lifecycle.repeatOnLifecycle(minimum) { events.collect { } }
                                                           ~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/test.kt:28: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle).debounce(300).collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
    }

    @Test
    fun `flowWithLifecycle 뒤에서 중단하는 변환은 보호되지 않는다`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.lifecycle.flowWithLifecycle
                import androidx.lifecycle.Lifecycle
                import kotlinx.coroutines.delay
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.map

                fun HomeRoute(events: Flow<String>, lifecycle: Lifecycle) {
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).map { delay(300); it }.collect { }
                    }
                    LaunchedEffect(Unit) {
                        events.flowWithLifecycle(lifecycle).map { it.length }.collect { }
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/test.kt:12: Error: LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요. [LifecycleUnawareEventCollect]
                    events.flowWithLifecycle(lifecycle).map { delay(300); it }.collect { }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }
}
