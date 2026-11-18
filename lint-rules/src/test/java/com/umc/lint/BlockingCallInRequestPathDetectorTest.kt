package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class BlockingCallInRequestPathDetectorTest {

    @Test
    fun `인터셉터 안의 runBlocking은 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/AuthenticationInterceptor.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class AuthenticationInterceptor(private val loadToken: suspend () -> String) {
                    fun token(): String = runBlocking { loadToken() }
                }
                """,
            ).indented(),
        ).expectErrorCount(1).expectContains("AuthenticationInterceptor.kt")
    }

    @Test
    fun `요청 경로가 아닌 파일의 runBlocking은 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/data/repository/TokenMigration.kt",
                """
                package com.umc.data.repository

                import kotlinx.coroutines.runBlocking

                fun migrate(load: suspend () -> String): String = runBlocking { load() }
                """,
            ).indented(),
        ).expectClean()
    }
}
