package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression

/**
 * 네트워크 요청 경로(인터셉터·인증자·요청 빌더)에서 runBlocking 사용을 금지한다.
 *
 * OkHttp Interceptor / Authenticator, Ktor 의 defaultRequest 블록은 요청을 시작한 스레드에서
 * 실행된다. ViewModel 이 대부분 메인 디스패처에서 UseCase 를 호출하므로, 이 경로의 runBlocking
 * 은 모든 API 호출마다 메인 스레드를 디스크 I/O 시간만큼 정지시킨다.
 *
 * 토큰 적재는 suspend 경로(Ktor Auth 의 loadTokens, OkHttp 라면 미리 캐시한 값)로 옮긴다.
 */
class BlockingCallInRequestPathDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("runBlocking")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.containingClass?.qualifiedName?.startsWith("kotlinx.coroutines") != true) return
        if (!isInRequestCallback(context, node)) return

        val fileName = context.file.name
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "요청 경로($fileName)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. " +
                "토큰 적재를 suspend 경로로 옮기세요.",
        )
    }

    /**
     * 요청마다 실행되는 코드 안인지 확인한다. 파일 이름이 아니라 실제 콜백을 본다.
     * - Interceptor·Authenticator를 구현한 클래스(intercept와 그 안에서 부르는 보조 함수 포함)
     * - Interceptor { … } 처럼 람다로 넘긴 콜백
     * - Ktor defaultRequest { … } 블록
     * NetworkModule의 앱 시작 초기화처럼 한 번만 도는 코드는 여기에 해당하지 않는다.
     */
    private fun isInRequestCallback(context: JavaContext, node: UElement): Boolean {
        var cursor: UElement? = node.uastParent
        while (cursor != null) {
            when (cursor) {
                is ULambdaExpression -> {
                    val type = (cursor.functionalInterfaceType as? PsiClassType)?.resolve()?.qualifiedName
                    if (type in REQUEST_CALLBACK_TYPES) return true
                    // OkHttp 4 의 addInterceptor { chain -> } 는 SAM 이 아니라 Kotlin 함수 타입을 받는다.
                    val owner = cursor.uastParent as? UCallExpression
                    if (owner != null && OKHTTP_BUILDER_CALLBACKS.any { context.calls(owner, it, "okhttp3") }) return true
                }
                is UCallExpression ->
                    if (context.calls(cursor, "defaultRequest", "io.ktor.client.plugins")) return true
                is UClass ->
                    return REQUEST_CALLBACK_TYPES.any { context.evaluator.inheritsFrom(cursor, it, false) }
            }
            cursor = cursor.uastParent
        }
        return false
    }

    companion object {
        private val REQUEST_CALLBACK_TYPES = setOf("okhttp3.Interceptor", "okhttp3.Authenticator")
        private val OKHTTP_BUILDER_CALLBACKS = listOf("addInterceptor", "addNetworkInterceptor", "authenticator")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "BlockingCallInRequestPath",
            briefDescription = "요청 경로에서 runBlocking 사용",
            explanation = """
                OkHttp Interceptor/Authenticator 와 Ktor defaultRequest 는 요청을 시작한 스레드 \
                (대개 메인)에서 실행됩니다. 이 경로에서 runBlocking 으로 DataStore 등을 읽으면 \
                모든 API 호출이 디스크 I/O 만큼 메인 스레드를 정지시킵니다.

                토큰 적재는 suspend 를 지원하는 경로(Ktor Auth 의 loadTokens 등)로 옮기거나, \
                메모리에 캐시된 값을 사용하세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                BlockingCallInRequestPathDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
