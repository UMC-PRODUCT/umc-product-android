package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class BlockingCallInRequestPathDetectorTest {

    @Test
    fun `Interceptor·Authenticator 구현 안의 runBlocking은 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/AuthenticationInterceptor.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Authenticator
                import okhttp3.Interceptor
                import okhttp3.Request
                import okhttp3.Response
                import okhttp3.Route

                class AuthenticationInterceptor(private val loadToken: suspend () -> String) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = runBlocking { loadToken() }
                        return chain.proceed(chain.request())
                    }
                }

                class TokenAuthenticator(private val refresh: suspend () -> Request?) : Authenticator {
                    override fun authenticate(route: Route?, response: Response): Request? = refreshBlocking()

                    private fun refreshBlocking(): Request? = runBlocking { refresh() }
                }
                """,
            ).indented(),
        ).expectErrorCount(2).expectContains("AuthenticationInterceptor.kt")
    }

    @Test
    fun `람다로 넘긴 인터셉터와 Ktor defaultRequest 안의 runBlocking은 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/NetworkModule.kt",
                """
                package com.umc.product.di

                import io.ktor.client.plugins.defaultRequest
                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.OkHttpClient

                fun provideClient(loadToken: suspend () -> String): OkHttpClient.Builder {
                    val auth = Interceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                    return OkHttpClient.Builder()
                        .addInterceptor(auth)
                        .addInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                        .addNetworkInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                }

                fun configureKtor(config: Any, loadToken: suspend () -> String) {
                    config.defaultRequest { header("Authorization", runBlocking { loadToken() }) }
                }
                """,
            ).indented(),
        ).expectErrorCount(4)
    }

    @Test
    fun `파일 이름만 요청 경로처럼 보이는 초기화 코드는 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/NetworkModule.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                object NetworkModule {
                    fun warmUpOnAppStart(load: suspend () -> String): String = runBlocking { load() }
                }
                """,
            ).indented(),
        ).expectClean()
    }
}
