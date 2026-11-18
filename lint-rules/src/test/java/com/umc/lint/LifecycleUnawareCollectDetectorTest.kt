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
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow

                fun HomeRoute(events: Flow<String>, scrollOffset: () -> Int, lifecycle: Any) {
                    LaunchedEffect(Unit) {
                        repeatOnLifecycle("STARTED") {
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
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.merge

                fun HomeRoute(events: Flow<String>, other: Flow<String>, lifecycle: Any) {
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
        ).expectErrorCount(2)
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
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.combine
                import kotlinx.coroutines.flow.distinctUntilChanged
                import kotlinx.coroutines.flow.flatMapLatest
                import kotlinx.coroutines.flow.map

                fun HomeRoute(events: Flow<String>, lifecycle: Any, offset: () -> Int) {
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
        ).expectErrorCount(2)
    }
}
