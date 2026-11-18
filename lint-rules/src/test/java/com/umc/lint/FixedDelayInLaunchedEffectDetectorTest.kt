package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class FixedDelayInLaunchedEffectDetectorTest {

    @Test
    fun `LaunchedEffect 안의 숫자 delay는 경고`() {
        lintWith(
            FixedDelayInLaunchedEffectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.community

                import androidx.compose.runtime.LaunchedEffect
                import kotlinx.coroutines.delay

                private const val KEYBOARD_SETTLE_MS = 120L

                fun ChattingScreen(focused: Boolean) {
                    LaunchedEffect(focused) {
                        delay(120)
                    }
                    LaunchedEffect(Unit) {
                        delay(KEYBOARD_SETTLE_MS)
                    }
                }
                """,
            ).indented(),
        ).expectWarningCount(2).expectContains("고정 시간(120ms)")
    }

    @Test
    fun `파라미터로 받은 시간과 LaunchedEffect 밖의 delay는 무시`() {
        lintWith(
            FixedDelayInLaunchedEffectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.community

                import androidx.compose.runtime.LaunchedEffect
                import kotlinx.coroutines.delay

                fun Toast(durationMillis: Long) {
                    LaunchedEffect(Unit) {
                        delay(durationMillis)
                    }
                }

                fun AutoDismiss(durationMillis: Long = 2_000L) {
                    LaunchedEffect(Unit) {
                        delay(durationMillis)
                    }
                }

                suspend fun debounceSearch() {
                    delay(300)
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `Duration으로 넘긴 고정 시간도 밀리초로 바꿔 경고`() {
        lintWith(
            FixedDelayInLaunchedEffectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.community

                import androidx.compose.runtime.LaunchedEffect
                import kotlinx.coroutines.delay
                import kotlin.time.Duration.Companion.milliseconds
                import kotlin.time.Duration.Companion.seconds

                private val KEYBOARD_SETTLE = 250.milliseconds

                fun ChattingScreen() {
                    LaunchedEffect(Unit) {
                        delay(150.milliseconds)
                        delay(2.seconds)
                        delay(KEYBOARD_SETTLE)
                    }
                    LaunchedEffect(Unit) {
                        val settle = 300.milliseconds
                        delay(settle)
                    }
                }
                """,
            ).indented(),
        ).expectWarningCount(4)
            .expectContains("고정 시간(150ms)")
            .expectContains("고정 시간(2000ms)")
            .expectContains("고정 시간(250ms)")
            .expectContains("고정 시간(300ms)")
    }

    @Test
    fun `파라미터나 var로 받은 Duration은 무시`() {
        lintWith(
            FixedDelayInLaunchedEffectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.community

                import androidx.compose.runtime.LaunchedEffect
                import kotlinx.coroutines.delay
                import kotlin.time.Duration
                import kotlin.time.Duration.Companion.milliseconds

                fun Retry(initial: Duration = 100.milliseconds) {
                    LaunchedEffect(Unit) {
                        delay(initial)
                        var backoff = 100.milliseconds
                        backoff *= 2
                        delay(backoff)
                    }
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `Compose가 아닌 동명 LaunchedEffect는 무시`() {
        lintWith(
            FixedDelayInLaunchedEffectDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.community

                import kotlinx.coroutines.delay
                import sample.LaunchedEffect

                fun Poller() {
                    LaunchedEffect(Unit) {
                        delay(120)
                    }
                }
                """,
            ).indented(),
        ).expectClean()
    }
}
