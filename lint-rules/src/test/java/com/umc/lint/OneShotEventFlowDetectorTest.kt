package com.umc.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class OneShotEventFlowDetectorTest {

    private val flowStub = kotlin(
        """
        package kotlinx.coroutines.flow
        interface MutableSharedFlow<T>
        fun <T> MutableSharedFlow(
            replay: Int = 0,
            extraBufferCapacity: Int = 0
        ): MutableSharedFlow<T> = TODO()
        """,
    ).indented()

    @Test
    fun `기본값 MutableSharedFlow 는 오류로 보고된다`() {
        lint()
            .allowMissingSdk()
            .files(
                flowStub,
                kotlin(
                    """
                    package com.umc.sample
                    import kotlinx.coroutines.flow.MutableSharedFlow
                    class SampleViewModel {
                        private val uiEvent = MutableSharedFlow<String>()
                    }
                    """,
                ).indented(),
            )
            .issues(OneShotEventFlowDetector.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `버퍼를 명시하면 의도된 설정으로 보고 통과시킨다`() {
        lint()
            .allowMissingSdk()
            .files(
                flowStub,
                kotlin(
                    """
                    package com.umc.sample
                    import kotlinx.coroutines.flow.MutableSharedFlow
                    class SampleViewModel {
                        private val state = MutableSharedFlow<String>(replay = 1)
                    }
                    """,
                ).indented(),
            )
            .issues(OneShotEventFlowDetector.ISSUE)
            .run()
            .expectClean()
    }
}
