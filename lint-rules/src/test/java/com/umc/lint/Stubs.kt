package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
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
        """,
    ).indented()

    private val lifecycle = kotlin(
        "src/androidx/lifecycle/Lifecycle.kt",
        """
        package androidx.lifecycle

        suspend fun repeatOnLifecycle(state: String, block: suspend () -> Unit) {}
        """,
    ).indented()

    private val coroutines = kotlin(
        "src/kotlinx/coroutines/Coroutines.kt",
        """
        package kotlinx.coroutines

        suspend fun delay(timeMillis: Long) {}
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
        """,
    ).indented()

    val ALL: Array<TestFile> = arrayOf(material3, foundationText, runtime, lifecycle, coroutines, flow)
}

/** [Stubs]를 함께 넣고 [issue] 하나만 검사한다. */
fun lintWith(issue: Issue, vararg files: TestFile): TestLintResult =
    lint()
        .allowMissingSdk()
        .files(*Stubs.ALL, *files)
        .issues(issue)
        .run()
