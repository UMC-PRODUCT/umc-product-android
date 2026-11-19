package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
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

                class InferredShadowInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    private fun cachedProvider(): () -> String = { "cached" }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val tokenProvider = cachedProvider()
                        tokenProvider()
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

    @Test
    fun `람다 프로퍼티를 읽기만 하거나 같은 이름의 메서드·다른 객체의 프로퍼티를 부르면 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/ReadOnlyInterceptors.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.Response

                class ReadOnlyInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val callback = tokenProvider
                        return chain.proceed(chain.request())
                    }
                }

                class Cached(val tokenProvider: () -> String)

                class ForeignReceiverInterceptor(private val other: Cached, private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        other.tokenProvider()
                        return chain.proceed(chain.request())
                    }
                }

                class ImplicitReceiverInterceptor(private val other: Cached, private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    override fun intercept(chain: Interceptor.Chain): Response {
                        with(other) { tokenProvider() }
                        return chain.proceed(chain.request())
                    }
                }

                class OverloadNameInterceptor(private val load: suspend () -> String) : Interceptor {
                    private val tokenProvider: () -> String = { runBlocking { load() } }
                    private fun tokenProvider(mode: Int): String = "cached"
                    override fun intercept(chain: Interceptor.Chain): Response {
                        tokenProvider(1)
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `같은 모듈의 다른 파일에서 콜백이 부르는 보조 함수도 오류, 콜백 밖에서만 부르면 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/TokenLoader.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                fun loadTokenBlocking(load: suspend () -> String): String = runBlocking { load() }

                fun warmUpBlocking(load: suspend () -> String): String = runBlocking { load() }

                private fun loadTokenBlocking(load: suspend () -> String, retry: Int): String = runBlocking { load() }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/RemoteInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class RemoteInterceptor(private val load: suspend () -> String) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        loadTokenBlocking(load)
                        return chain.proceed(chain.request())
                    }
                }

                object Startup {
                    fun init(load: suspend () -> String) {
                        warmUpBlocking(load)
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/TokenLoader.kt:5: Error: 요청 경로(TokenLoader.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
            fun loadTokenBlocking(load: suspend () -> String): String = runBlocking { load() }
                                                                        ~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `다른 파일에서 프로퍼티를 읽으면 getter, 쓰면 setter 의 runBlocking 만 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/TokenCache.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class TokenCache(private val save: suspend (String) -> Unit) {
                    var token: String = ""
                        private set(value) {
                            field = value
                            runBlocking { save(value) }
                        }

                    fun refresh(new: String) {
                        token = new
                    }
                }

                class SessionCache(private val load: suspend () -> String, private val save: suspend (String) -> Unit) {
                    var session: String
                        get() = runBlocking { load() }
                        set(value) {
                            runBlocking { save(value) }
                        }
                }

                class RetryCounter(private val load: suspend () -> Int) {
                    var count: Int
                        get() = runBlocking { load() }
                        set(value) {}
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/AuthInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class AuthInterceptor(
                    private val cache: TokenCache,
                    private val sessions: SessionCache,
                    private val retries: RetryCounter,
                ) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = cache.token
                        sessions.session = token
                        retries.count++
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/TokenCache.kt:21: Error: 요청 경로(TokenCache.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                        runBlocking { save(value) }
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenCache.kt:27: Error: 요청 경로(TokenCache.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    get() = runBlocking { load() }
                            ~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `다른 파일의 operator 호출과 Java 호출(getter·JvmName)도 찾는다`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/TokenSources.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class TokenProvider(private val load: suspend () -> String) {
                    operator fun invoke(): String = runBlocking { load() }
                }

                class TokenStore(private val load: suspend (String) -> String) {
                    operator fun get(key: String): String = runBlocking { load(key) }
                }

                class LegacyTokens(private val load: suspend () -> String) {
                    val legacyToken: String
                        get() = runBlocking { load() }

                    @JvmName("blockingFetch")
                    fun fetchToken(): String = runBlocking { load() }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/AuthInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class AuthInterceptor(private val provider: TokenProvider, private val store: TokenStore) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = provider()
                        val cached = store["token"]
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
            java(
                "src/com/umc/product/di/LegacyInterceptor.java",
                """
                package com.umc.product.di;

                import okhttp3.Interceptor;
                import okhttp3.Response;

                public class LegacyInterceptor implements Interceptor {
                    private final LegacyTokens tokens;

                    public LegacyInterceptor(LegacyTokens tokens) {
                        this.tokens = tokens;
                    }

                    @Override
                    public Response intercept(Interceptor.Chain chain) {
                        String token = tokens.getLegacyToken();
                        String fetched = tokens.blockingFetch();
                        return chain.proceed(chain.request());
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/TokenSources.kt:6: Error: 요청 경로(TokenSources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                operator fun invoke(): String = runBlocking { load() }
                                                ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenSources.kt:10: Error: 요청 경로(TokenSources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                operator fun get(key: String): String = runBlocking { load(key) }
                                                        ~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenSources.kt:15: Error: 요청 경로(TokenSources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    get() = runBlocking { load() }
                            ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenSources.kt:18: Error: 요청 경로(TokenSources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                fun fetchToken(): String = runBlocking { load() }
                                           ~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """,
        )
    }

    @Test
    fun `다른 파일에서 부르는 생성자 람다 프로퍼티와 함수 참조로 넘긴 보조 함수도 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/TokenHolder.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Request

                class TokenHolder(
                    val provide: () -> String = { runBlocking { "token" } },
                )

                fun attachToken(request: Request): Request = runBlocking { request }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/HolderInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Request
                import okhttp3.Response

                class HolderInterceptor(private val holder: TokenHolder) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        holder.provide()
                        return chain.proceed(transform(chain.request(), ::attachToken))
                    }

                    private fun transform(request: Request, block: (Request) -> Request): Request = block(request)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/TokenHolder.kt:7: Error: 요청 경로(TokenHolder.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                val provide: () -> String = { runBlocking { "token" } },
                                              ~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/TokenHolder.kt:10: Error: 요청 경로(TokenHolder.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
            fun attachToken(request: Request): Request = runBlocking { request }
                                                         ~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `키워드 없이 재정의한 operator 는 구체 타입 호출만 찾고, 라이브러리 상위 메서드로 해석된 다른 파일 호출은 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/Sources.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class TokenSource(private val load: suspend () -> String) : () -> String {
                    override fun invoke(): String = runBlocking { load() }
                }

                class StartupSource(private val load: suspend () -> String) : () -> String {
                    override fun invoke(): String = runBlocking { load() }
                }

                class TokenMap(private val load: suspend (String) -> String) : AbstractMap<String, String>() {
                    override val entries: Set<Map.Entry<String, String>> get() = emptySet()
                    override fun get(key: String): String? = runBlocking { load(key) }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/SourceInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class SourceInterceptor(private val source: TokenSource, private val tokens: TokenMap) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = source()
                        val cached = tokens["k"]
                        return chain.proceed(chain.request())
                    }
                }

                class UserAgentInterceptor(private val userAgent: () -> String) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val agent = userAgent()
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/Sources.kt:6: Error: 요청 경로(Sources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                override fun invoke(): String = runBlocking { load() }
                                                ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/Sources.kt:15: Error: 요청 경로(Sources.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                override fun get(key: String): String? = runBlocking { load(key) }
                                                         ~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }

    @Test
    fun `복합 대입은 인덱스 set 까지 찾고, plusAssign 으로 풀리는 += 는 setter 를 부르지 않는다`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/Stores.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class CounterStore(private val save: suspend (Int) -> Unit) {
                    operator fun get(key: String): Int = 0
                    operator fun set(key: String, value: Int) {
                        runBlocking { save(value) }
                    }
                }

                class Acc {
                    operator fun plusAssign(n: Int) {}
                }

                class AccHolder(private val save: suspend (Acc) -> Unit) {
                    var acc: Acc = Acc()
                        set(value) {
                            field = value
                            runBlocking { save(value) }
                        }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/StoreInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class StoreInterceptor(private val counters: CounterStore, private val holder: AccHolder) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        counters["k"] += 1
                        holder.acc += 1
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/Stores.kt:8: Error: 요청 경로(Stores.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    runBlocking { save(value) }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `로컬 함수 참조, 다른 파일에서 부르는 생성자와 Java 인터페이스 구현도 오류`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/LocalRef.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Interceptor
                import okhttp3.Request

                fun provideInterceptor(): Interceptor {
                    fun localAttach(request: Request): Request = runBlocking { request }
                    return Interceptor { chain -> chain.proceed(transform(chain.request(), ::localAttach)) }
                }

                fun transform(request: Request, block: (Request) -> Request): Request = block(request)
                """,
            ).indented(),
            java(
                "src/com/umc/product/di/JSource.java",
                """
                package com.umc.product.di;

                public interface JSource {
                    String jToken();
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/Impls.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking
                import okhttp3.Request

                private class DiskSource : JSource {
                    override fun jToken(): String = runBlocking { "j" }
                }

                class SignedRequest(val request: Request) {
                    constructor(request: Request, retry: Int) : this(request) {
                        runBlocking { retry }
                    }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/ImplInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class ImplInterceptor(private val source: JSource) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = source.jToken()
                        val signed = SignedRequest(chain.request(), 1)
                        return chain.proceed(signed.request)
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/Impls.kt:7: Error: 요청 경로(Impls.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                override fun jToken(): String = runBlocking { "j" }
                                                ~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/Impls.kt:12: Error: 요청 경로(Impls.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    runBlocking { retry }
                    ~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/LocalRef.kt:8: Error: 요청 경로(LocalRef.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                fun localAttach(request: Request): Request = runBlocking { request }
                                                             ~~~~~~~~~~~~~~~~~~~~~~~
            3 errors
            """,
        )
    }

    @Test
    fun `생성자 상위 프로퍼티도 접근 방향을 보고, Java 가 람다 프로퍼티를 꺼내기만 하면 무시`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/Holders.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                abstract class TokenHolderBase(open var token: String)

                class DiskTokenHolder(private val save: suspend (String) -> Unit) : TokenHolderBase("") {
                    override var token: String = ""
                        set(value) {
                            field = value
                            runBlocking { save(value) }
                        }
                }

                class ReadOnlyHolder(val provide: () -> String = { runBlocking { "read" } })

                class InvokedHolder(val provide: () -> String = { runBlocking { "invoked" } })
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/BaseInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class BaseInterceptor(private val holder: TokenHolderBase) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = holder.token
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
            java(
                "src/com/umc/product/di/HolderJavaInterceptor.java",
                """
                package com.umc.product.di;

                import okhttp3.Interceptor;
                import okhttp3.Response;

                public class HolderJavaInterceptor implements Interceptor {
                    private final ReadOnlyHolder readOnly;
                    private final InvokedHolder invoked;

                    public HolderJavaInterceptor(ReadOnlyHolder readOnly, InvokedHolder invoked) {
                        this.readOnly = readOnly;
                        this.invoked = invoked;
                    }

                    @Override
                    public Response intercept(Interceptor.Chain chain) {
                        Object provider = readOnly.getProvide();
                        String token = invoked.getProvide().invoke();
                        return chain.proceed(chain.request());
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/Holders.kt:17: Error: 요청 경로(Holders.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
            class InvokedHolder(val provide: () -> String = { runBlocking { "invoked" } })
                                                              ~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `복합 대입은 실제로 불리는 set 만 찾는다(오버로드, 원소 plusAssign, 하위 클래스 set)`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            kotlin(
                "src/com/umc/product/di/Indexed.kt",
                """
                package com.umc.product.di

                import kotlinx.coroutines.runBlocking

                class Prefs(private val save: suspend (Any) -> Unit) {
                    operator fun get(key: String): Int = 0
                    operator fun set(key: String, value: Int) {}
                    operator fun set(key: String, value: String) {
                        runBlocking { save(value) }
                    }
                }

                class Acc {
                    operator fun plusAssign(n: Int) {}
                }

                class Registry(private val save: suspend (Acc) -> Unit) {
                    operator fun get(key: String): Acc = Acc()
                    operator fun set(key: String, value: Acc) {
                        runBlocking { save(value) }
                    }
                }

                open class BaseStore {
                    operator fun get(key: String): Int = 0
                }

                class DerivedStore(private val save: suspend (Int) -> Unit) : BaseStore() {
                    operator fun set(key: String, value: Int) {
                        runBlocking { save(value) }
                    }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/IndexedInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class IndexedInterceptor(
                    private val prefs: Prefs,
                    private val registry: Registry,
                    private val derived: DerivedStore,
                ) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        prefs["k"] += 1
                        registry["k"] += 1
                        derived["k"] += 1
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/Indexed.kt:30: Error: 요청 경로(Indexed.kt)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    runBlocking { save(value) }
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `Kotlin 이 프로퍼티 문법이나 operator 로 부르는 Java 메서드도 찾는다`() {
        lintWith(
            BlockingCallInRequestPathDetector.ISSUE,
            java(
                "src/com/umc/product/di/JLoader.java",
                """
                package com.umc.product.di;

                public class JLoader {
                    public String getToken() {
                        return (String) kotlinx.coroutines.CoroutinesKt.runBlocking(null);
                    }
                }
                """,
            ).indented(),
            java(
                "src/com/umc/product/di/JStore.java",
                """
                package com.umc.product.di;

                public class JStore {
                    public String get(String key) {
                        return (String) kotlinx.coroutines.CoroutinesKt.runBlocking(null);
                    }
                }
                """,
            ).indented(),
            kotlin(
                "src/com/umc/product/di/JavaUseInterceptor.kt",
                """
                package com.umc.product.di

                import okhttp3.Interceptor
                import okhttp3.Response

                class JavaUseInterceptor(private val loader: JLoader, private val store: JStore) : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val token = loader.token
                        val cached = store["k"]
                        return chain.proceed(chain.request())
                    }
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/product/di/JLoader.java:5: Error: 요청 경로(JLoader.java)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    return (String) kotlinx.coroutines.CoroutinesKt.runBlocking(null);
                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/product/di/JStore.java:5: Error: 요청 경로(JStore.java)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. 토큰 적재를 suspend 경로로 옮기세요. [BlockingCallInRequestPath]
                    return (String) kotlinx.coroutines.CoroutinesKt.runBlocking(null);
                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }
}
