package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class OneShotEventFlowDetectorTest {

    @Test
    fun `인자 없는 MutableSharedFlow는 오류`() {
        lintWith(
            OneShotEventFlowDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.notice

                import kotlinx.coroutines.flow.MutableSharedFlow

                class NoticeViewModel {
                    val events = MutableSharedFlow<Unit>()
                }
                """,
            ).indented(),
        ).expectErrorCount(1).expectContains("Channel(Channel.BUFFERED)")
    }

    @Test
    fun `버퍼를 지정한 MutableSharedFlow는 허용`() {
        lintWith(
            OneShotEventFlowDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.notice

                import kotlinx.coroutines.flow.MutableSharedFlow

                class NoticeViewModel {
                    val events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
                }
                """,
            ).indented(),
        ).expectClean()
    }
}
