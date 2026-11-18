package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class BlockingCallInRequestPathDetectorTest {

    @Test
    fun `intercept·authenticate 본문과 거기서 부르는 보조 함수만 오류`() {
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
                    private val warmedUp = runBlocking { loadToken() }

                    init {
                        runBlocking { loadToken() }
                    }

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
        ).expect(
            """
            src/com/umc/product/di/AuthenticationInterceptor.kt:18: Error: 요청 경로(AuthenticationInterceptor.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    val token = runBlocking { loadToken() }
                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/AuthenticationInterceptor.kt:26: Error: 요청 경로(AuthenticationInterceptor.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                private fun refreshBlocking(): Request? = runBlocking { refresh() }
                                                          ~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `콜백 람다 본문과 그 람다가 부르는 같은 파일의 보조 함수는 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/NetworkModule.kt",
                """
                package com.umc.product.di

                import io.ktor.client.HttpClientConfig
                import io.ktor.client.plugins.DefaultRequest
                import io.ktor.client.plugins.defaultRequest
                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.OkHttpClient

                private fun loadTokenBlocking(load: suspend () -> String): String = runBlocking { load() }

                fun provideClient(loadToken: suspend () -> String): OkHttpClient.Builder {
                    val auth = Interceptor { chain -> loadTokenBlocking(loadToken); chain.proceed(chain.request()) }
                    return OkHttpClient.Builder()
                        .addInterceptor(auth)
                        .addInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                        .addNetworkInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                }

                fun configureKtor(config: HttpClientConfig<*>, loadToken: suspend () -> String) {
                    config.defaultRequest { header("Authorization", runBlocking { loadToken() }) }
                }

                private fun makeDefaults(token: String): DefaultRequest.DefaultRequestBuilder.() -> Unit =
                    { header("Authorization", token) }

                fun configureKtorOnce(config: HttpClientConfig<*>, loadToken: suspend () -> String) {
                    config.defaultRequest(makeDefaults(runBlocking { loadToken() }))
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/NetworkModule.kt:10: Error: 요청 경로(NetworkModule.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
            private fun loadTokenBlocking(load: suspend () -> String): String = runBlocking { load() }
                                                                                ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/NetworkModule.kt:16: Error: 요청 경로(NetworkModule.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    .addInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/NetworkModule.kt:17: Error: 요청 경로(NetworkModule.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    .addNetworkInterceptor { chain -> runBlocking { loadToken() }; chain.proceed(chain.request()) }
                                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/NetworkModule.kt:21: Error: 요청 경로(NetworkModule.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                config.defaultRequest { header("Authorization", runBlocking { loadToken() }) }
                                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
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

    @Test
    fun `요청마다 도는 getter·재정의한 훅·로컬 함수·람다 프로퍼티도 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/TokenInterceptors.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.Response

                class GetterInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val accessToken: String get() = runBlocking { load() }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = accessToken
                        return chain.proceed(chain.request())
                    }
                }

                abstract class TokenInterceptor : Interceptor {
                    abstract fun token(): String
                    override fun intercept(chain: Interceptor.Chain): Response {
                        token()
                        return chain.proceed(chain.request())
                    }
                }

                class AppTokenInterceptor(private val load: suspend () -> String) : TokenInterceptor() {
                    override fun token(): String = runBlocking { load() }
                }

                class LambdaPropertyInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        tokenProvider()
                        return chain.proceed(chain.request())
                    }
                }

                fun provideInterceptor(load: suspend () -> String): Interceptor {
                    fun tokenBlocking(): String = runBlocking { load() }
                    return Interceptor { chain -> tokenBlocking(); chain.proceed(chain.request()) }
                }

                fun warmUp(load: suspend () -> String) {
                    fun tokenBlocking(): String = runBlocking { load() }
                    tokenBlocking()
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/TokenInterceptors.kt:8: Error: 요청 경로(TokenInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                private val accessToken: String get() = runBlocking { load() }
                                                        ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenInterceptors.kt:24: Error: 요청 경로(TokenInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                override fun token(): String = runBlocking { load() }
                                               ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenInterceptors.kt:28: Error: 요청 경로(TokenInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                private val tokenProvider: () -> String = { runBlocking { load() } }
                                                            ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenInterceptors.kt:36: Error: 요청 경로(TokenInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                fun tokenBlocking(): String = runBlocking { load() }
                                              ~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
    }

    @Test
    fun `이름만 같은 오버로드·다른 파일의 재정의·가려진 동명 람다는 무시하고 짧은 호출 경로는 찾는다`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/BaseInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                abstract class BaseInterceptor : Interceptor {
                    abstract fun warmUp()
                    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/AppInterceptors.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.Response

                class WarmUpInterceptor(private val load: suspend () -> String) : BaseInterceptor() {
                    override fun warmUp() {
                        runBlocking { load() }
                    }
                }

                class OverloadInterceptor(private val load: suspend () -> String) : Interceptor {
                    private fun intercept() {
                        runBlocking { load() }
                    }
                    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
                }

                class ShadowInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val tokenProvider = { "cached" }
                        tokenProvider()
                        return chain.proceed(chain.request())
                    }
                }

                class ChainInterceptor(private val load: suspend () -> String) : Interceptor {
                    private fun a(): String = t()
                    private fun b(): String = a()
                    private fun t(): String = runBlocking { load() }
                    private fun x(): String = b() + t()
                    private fun y(): String = x()
                    override fun intercept(chain: Interceptor.Chain): Response {
                        y()
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/AppInterceptors.kt:32: Error: 요청 경로(AppInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                private fun t(): String = runBlocking { load() }
                                          ~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `자기 초기화식 안의 같은 이름 호출과 다른 파일 기반 클래스가 부르는 훅도 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/BaseTokenInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                abstract class BaseTokenInterceptor : Interceptor {
                    abstract fun token(): String
                    override fun intercept(chain: Interceptor.Chain): Response {
                        token()
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/SelfNamedInterceptors.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.Response

                class AppTokenInterceptor(private val load: suspend () -> String) : BaseTokenInterceptor() {
                    override fun token(): String = runBlocking { load() }
                }

                class SelfNamedInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val accessToken: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val accessToken = accessToken()
                        return chain.proceed(chain.request())
                    }
                }

                fun provide(load: suspend () -> String): Interceptor {
                    fun token(): String = runBlocking { load() }
                    return Interceptor { chain ->
                        val token: String = token()
                        chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/SelfNamedInterceptors.kt:8: Error: 요청 경로(SelfNamedInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                override fun token(): String = runBlocking { load() }
                                               ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/SelfNamedInterceptors.kt:12: Error: 요청 경로(SelfNamedInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                private val accessToken: () -> String = { runBlocking { load() } }
                                                          ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/SelfNamedInterceptors.kt:20: Error: 요청 경로(SelfNamedInterceptors.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                fun token(): String = runBlocking { load() }
                                      ~~~~~~~~~~~~~~~~~~~~~~
            3 errors
            """,
        )
    }
}
