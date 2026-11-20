package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Issue

/** 규칙 테스트에 필요한 라이브러리 API의 최소 시그니처. 실제 라이브러리 대신 패키지와 이름만 맞춘다. */
object Stubs {
    private val material3 = kotlin(
        "src/androidx/compose/material3/Material3.kt",
        """
        package androidx.compose.material3

        fun Text(text: String) {}
        fun TextField(value: String, onValueChange: (String) -> Unit) {}
        fun OutlinedTextField(value: String, onValueChange: (String) -> Unit) {}
        """,
    ).indented()

    private val foundationText = kotlin(
        "src/androidx/compose/foundation/text/FoundationText.kt",
        """
        package androidx.compose.foundation.text

        fun BasicText(text: String) {}
        fun BasicTextField(value: String, onValueChange: (String) -> Unit) {}
        """,
    ).indented()

    private val runtime = kotlin(
        "src/androidx/compose/runtime/Runtime.kt",
        """
        package androidx.compose.runtime

        import kotlinx.coroutines.flow.Flow

        fun LaunchedEffect(key1: Any?, block: suspend () -> Unit) {}
        fun <T> snapshotFlow(block: () -> T): Flow<T> = throw UnsupportedOperationException()

        annotation class Composable
        annotation class Stable
        annotation class Immutable
        """,
    ).indented()

    private val ui = kotlin(
        "src/androidx/compose/ui/Modifier.kt",
        """
        package androidx.compose.ui

        interface Modifier {
            infix fun then(other: Modifier): Modifier = other

            companion object : Modifier
        }
        """,
    ).indented()

    private val unit = kotlin(
        "src/androidx/compose/ui/unit/Dp.kt",
        """
        package androidx.compose.ui.unit

        @JvmInline
        value class Dp(val value: Float)

        val Int.dp: Dp get() = Dp(this.toFloat())
        val Double.dp: Dp get() = Dp(this.toFloat())
        val Float.dp: Dp get() = Dp(this)

        @JvmInline
        value class DpSize(val packed: Long)

        fun DpSize(width: Dp, height: Dp): DpSize = DpSize(0L)
        """,
    ).indented()

    private val layout = kotlin(
        "src/androidx/compose/foundation/layout/Layout.kt",
        """
        package androidx.compose.foundation.layout

        import androidx.compose.ui.Modifier
        import androidx.compose.ui.unit.Dp
        import androidx.compose.ui.unit.DpSize

        fun Modifier.width(width: Dp): Modifier = this
        fun Modifier.size(size: DpSize): Modifier = this
        fun Modifier.wrapContentWidth(): Modifier = this
        fun Modifier.requiredWidth(width: Dp): Modifier = this
        fun Modifier.size(size: Dp): Modifier = this
        fun Modifier.size(width: Dp, height: Dp): Modifier = this
        fun Modifier.requiredSize(size: Dp): Modifier = this
        fun Modifier.widthIn(min: Dp = Dp(-1f), max: Dp = Dp(-1f)): Modifier = this
        fun Modifier.requiredWidthIn(min: Dp = Dp(-1f), max: Dp = Dp(-1f)): Modifier = this
        fun Modifier.sizeIn(minWidth: Dp = Dp(-1f), minHeight: Dp = Dp(-1f), maxWidth: Dp = Dp(-1f), maxHeight: Dp = Dp(-1f)): Modifier = this
        fun Modifier.defaultMinSize(minWidth: Dp = Dp(-1f), minHeight: Dp = Dp(-1f)): Modifier = this
        fun Modifier.fillMaxWidth(fraction: Float = 1f): Modifier = this
        fun Modifier.fillMaxSize(fraction: Float = 1f): Modifier = this
        fun Modifier.padding(horizontal: Dp = Dp(0f), vertical: Dp = Dp(0f)): Modifier = this
        """,
    ).indented()

    private val preview = kotlin(
        "src/androidx/compose/ui/tooling/preview/Preview.kt",
        """
        package androidx.compose.ui.tooling.preview

        annotation class Preview
        annotation class PreviewScreenSizes
        """,
    ).indented()

    private val immutable = kotlin(
        "src/kotlinx/collections/immutable/Immutable.kt",
        """
        package kotlinx.collections.immutable

        interface ImmutableCollection<out E> : Collection<E>
        interface ImmutableList<out E> : List<E>, ImmutableCollection<E>
        interface ImmutableSet<out E> : Set<E>, ImmutableCollection<E>
        interface ImmutableMap<K, out V> : Map<K, V>
        """,
    ).indented()

    private val lifecycle = kotlin(
        "src/androidx/lifecycle/Lifecycle.kt",
        """
        package androidx.lifecycle

        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.flow.Flow

        abstract class Lifecycle {
            enum class State { DESTROYED, INITIALIZED, CREATED, STARTED, RESUMED }
        }

        suspend fun Lifecycle.repeatOnLifecycle(state: Lifecycle.State, block: suspend CoroutineScope.() -> Unit) {}
        fun <T> Flow<T>.flowWithLifecycle(lifecycle: Lifecycle, minActiveState: Lifecycle.State = Lifecycle.State.STARTED): Flow<T> = this
        """,
    ).indented()

    private val coroutines = kotlin(
        "src/kotlinx/coroutines/Coroutines.kt",
        """
        package kotlinx.coroutines

        interface CoroutineScope

        suspend fun delay(timeMillis: Long) {}
        suspend fun delay(duration: kotlin.time.Duration) {}
        fun <T> runBlocking(block: suspend () -> T): T = throw UnsupportedOperationException()
        """,
    ).indented()

    private val flow = kotlin(
        "src/kotlinx/coroutines/flow/Flow.kt",
        """
        package kotlinx.coroutines.flow

        interface Flow<out T> {
            suspend fun collect(collector: FlowCollector<T>)
        }

        fun interface FlowCollector<in T> {
            suspend fun emit(value: T)
        }

        interface MutableSharedFlow<T> : Flow<T>

        fun <T> MutableSharedFlow(replay: Int = 0, extraBufferCapacity: Int = 0): MutableSharedFlow<T> =
            throw UnsupportedOperationException()

        suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit) {}

        fun <T> merge(vararg flows: Flow<T>): Flow<T> = throw UnsupportedOperationException()
        fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R> = throw UnsupportedOperationException()
        fun <T> Flow<T>.distinctUntilChanged(): Flow<T> = this
        fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> = this
        fun <T> Flow<T>.onStart(action: suspend FlowCollector<T>.() -> Unit): Flow<T> = this
        fun <T> Flow<T>.retryWhen(predicate: suspend FlowCollector<T>.(cause: Throwable, attempt: Long) -> Boolean): Flow<T> = this
        fun <T1, T2, R> Flow<T1>.combine(other: Flow<T2>, transform: suspend (T1, T2) -> R): Flow<R> =
            throw UnsupportedOperationException()
        fun <T, R> Flow<T>.flatMapLatest(transform: suspend (T) -> Flow<R>): Flow<R> =
            throw UnsupportedOperationException()
        """,
    ).indented()

    private val okhttp = kotlin(
        "src/okhttp3/OkHttp.kt",
        """
        package okhttp3

        class Request
        class Response
        class Route

        fun interface Interceptor {
            fun intercept(chain: Chain): Response

            interface Chain {
                fun request(): Request
                fun proceed(request: Request): Response
            }
        }

        fun interface Authenticator {
            fun authenticate(route: Route?, response: Response): Request?
        }

        class OkHttpClient {
            // 실제 OkHttp 처럼 함수 타입 오버로드는 JVM 이름이 "-addInterceptor" 다. 이 이름으로 해석되는 상황을 재현한다.
            class Builder {
                fun addInterceptor(interceptor: Interceptor): Builder = this
                @JvmName("-addInterceptor")
                inline fun addInterceptor(crossinline block: (chain: Interceptor.Chain) -> Response): Builder = this
                fun addNetworkInterceptor(interceptor: Interceptor): Builder = this
                @JvmName("-addNetworkInterceptor")
                inline fun addNetworkInterceptor(crossinline block: (chain: Interceptor.Chain) -> Response): Builder = this
                fun authenticator(authenticator: Authenticator): Builder = this
            }
        }
        """,
    ).indented()

    private val ktorClient = kotlin(
        "src/io/ktor/client/HttpClientConfig.kt",
        """
        package io.ktor.client

        class HttpClientConfig<T>
        """,
    ).indented()

    private val ktor = kotlin(
        "src/io/ktor/client/plugins/DefaultRequest.kt",
        """
        package io.ktor.client.plugins

        import io.ktor.client.HttpClientConfig

        class DefaultRequest {
            class DefaultRequestBuilder {
                fun header(key: String, value: String) {}
            }
        }

        fun HttpClientConfig<*>.defaultRequest(block: DefaultRequest.DefaultRequestBuilder.() -> Unit) {}
        """,
    ).indented()

    /** Compose 것이 아닌 동명 함수. 이름만 보고 예외 처리하면 안 되는 경우를 만든다. */
    private val lookalikes = kotlin(
        "src/sample/Lookalikes.kt",
        """
        package sample

        import kotlinx.coroutines.flow.Flow

        fun snapshotFlow(): Flow<String> = throw UnsupportedOperationException()
        fun LaunchedEffect(key1: Any?, block: suspend () -> Unit) {}
        """,
    ).indented()

    val ALL: Array<TestFile> = arrayOf(
        material3, foundationText, runtime, ui, unit, layout, preview, immutable,
        lifecycle, coroutines, flow, okhttp, ktorClient, ktor, lookalikes,
    )

    /** :presentation:component 모듈처럼 namespace가 com.umc.component인 모듈. */
    val componentModuleManifest: TestFile = manifest(
        """<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.umc.component" />""",
    )

    /** 화면 모듈. */
    val screenModuleManifest: TestFile = manifest(
        """<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.umc.presentation.home" />""",
    )
}

/** [Stubs]를 함께 넣고 [issue] 하나만 검사한다. */
fun lintWith(issue: Issue, vararg files: TestFile): TestLintResult =
    lint()
        .allowMissingSdk()
        .files(*Stubs.ALL, *files)
        .issues(issue)
        .run()
