package com.umc.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class LifecycleUnawareCollectDetectorTest {

    private val stubs = arrayOf(
        kotlin(
            """
            package kotlinx.coroutines.flow
            interface Flow<T>
            suspend fun <T> Flow<T>.collect(action: (T) -> Unit) {}
            suspend fun <T> Flow<T>.collectLatest(action: (T) -> Unit) {}
            """,
        ).indented(),
        kotlin(
            """
            package androidx.compose.runtime
            fun LaunchedEffect(key: Any?, block: suspend () -> Unit) {}
            """,
        ).indented(),
        kotlin(
            """
            package androidx.lifecycle.compose
            suspend fun repeatOnLifecycle(state: Any, block: suspend () -> Unit) {}
            """,
        ).indented(),
    )

    @Test
    fun `LaunchedEffect 안의 맨몸 수집은 경고로 보고된다`() {
        lint()
            .allowMissingSdk()
            .files(
                *stubs,
                kotlin(
                    """
                    package com.umc.sample
                    import androidx.compose.runtime.LaunchedEffect
                    import kotlinx.coroutines.flow.Flow
                    import kotlinx.coroutines.flow.collect
                    fun Screen(events: Flow<String>) {
                        LaunchedEffect(Unit) {
                            events.collect { }
                        }
                    }
                    """,
                ).indented(),
            )
            .issues(LifecycleUnawareCollectDetector.ISSUE)
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun `repeatOnLifecycle 로 감싸면 통과시킨다`() {
        lint()
            .allowMissingSdk()
            .files(
                *stubs,
                kotlin(
                    """
                    package com.umc.sample
                    import androidx.compose.runtime.LaunchedEffect
                    import androidx.lifecycle.compose.repeatOnLifecycle
                    import kotlinx.coroutines.flow.Flow
                    import kotlinx.coroutines.flow.collect
                    fun Screen(events: Flow<String>) {
                        LaunchedEffect(Unit) {
                            repeatOnLifecycle(Unit) {
                                events.collect { }
                            }
                        }
                    }
                    """,
                ).indented(),
            )
            .issues(LifecycleUnawareCollectDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `snapshotFlow 로 상태를 관찰하는 것은 대상이 아니다`() {
        lint()
            .allowMissingSdk()
            .files(
                *stubs,
                kotlin(
                    """
                    package androidx.compose.runtime
                    import kotlinx.coroutines.flow.Flow
                    fun <T> snapshotFlow(block: () -> T): Flow<T> = TODO()
                    """,
                ).indented(),
                kotlin(
                    """
                    package com.umc.sample
                    import androidx.compose.runtime.LaunchedEffect
                    import androidx.compose.runtime.snapshotFlow
                    import kotlinx.coroutines.flow.collect
                    fun Screen(index: Int) {
                        LaunchedEffect(Unit) {
                            snapshotFlow { index }.collect { }
                        }
                    }
                    """,
                ).indented(),
            )
            .issues(LifecycleUnawareCollectDetector.ISSUE)
            .testModes(com.android.tools.lint.checks.infrastructure.TestMode.DEFAULT)
            .run()
            .expectClean()
    }
}
