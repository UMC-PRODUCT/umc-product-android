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
}
