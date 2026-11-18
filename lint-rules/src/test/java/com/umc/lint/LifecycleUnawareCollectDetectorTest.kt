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
    fun `repeatOnLifecycle 안의 수집과 snapshotFlow 수집은 허용`() {
        lintWith(
            LifecycleUnawareCollectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.LaunchedEffect
                import androidx.compose.runtime.snapshotFlow
                import androidx.lifecycle.repeatOnLifecycle
                import kotlinx.coroutines.flow.Flow

                fun HomeRoute(events: Flow<String>, scrollOffset: () -> Int) {
                    LaunchedEffect(Unit) {
                        repeatOnLifecycle("STARTED") {
                            events.collect { }
                        }
                    }
                    LaunchedEffect(Unit) {
                        snapshotFlow { scrollOffset() }.collect { }
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
}
