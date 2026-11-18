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
}
