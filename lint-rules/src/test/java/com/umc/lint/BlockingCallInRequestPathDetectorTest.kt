package com.umc.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class BlockingCallInRequestPathDetectorTest {

    private val coroutinesStub = kotlin(
        """
        package kotlinx.coroutines
        fun <T> runBlocking(block: () -> T): T = block()
        """,
    ).indented()

    @Test
    fun `인터셉터의 runBlocking 은 경고로 보고된다`() {
        lint()
            .allowMissingSdk()
            .files(
                coroutinesStub,
                kotlin(
                    "src/com/umc/sample/AuthenticationInterceptor.kt",
                    """
                    package com.umc.sample
                    import kotlinx.coroutines.runBlocking
                    class AuthenticationInterceptor {
                        fun intercept(): String = runBlocking { "token" }
                    }
                    """,
                ).indented(),
            )
            .issues(BlockingCallInRequestPathDetector.ISSUE)
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun `요청 경로가 아닌 곳은 통과시킨다`() {
        lint()
            .allowMissingSdk()
            .files(
                coroutinesStub,
                kotlin(
                    "src/com/umc/sample/MigrationTool.kt",
                    """
                    package com.umc.sample
                    import kotlinx.coroutines.runBlocking
                    class MigrationTool {
                        fun run(): String = runBlocking { "done" }
                    }
                    """,
                ).indented(),
            )
            .issues(BlockingCallInRequestPathDetector.ISSUE)
            .run()
            .expectClean()
    }
}
