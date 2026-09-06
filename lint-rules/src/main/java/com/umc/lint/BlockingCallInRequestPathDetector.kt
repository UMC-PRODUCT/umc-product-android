package com.umc.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

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

        val fileName = context.file.name
        val isRequestPath = REQUEST_PATH_MARKERS.any { fileName.contains(it, ignoreCase = true) }
        if (!isRequestPath) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "요청 경로($fileName)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. " +
                "토큰 적재를 suspend 경로로 옮기세요.",
        )
    }

    companion object {
        private val REQUEST_PATH_MARKERS = listOf(
            "Interceptor", "Authenticator", "HttpClient", "NetworkModule", "ApiModule",
        )

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
            category = Category.PERFORMANCE,
            priority = 8,
            severity = Severity.WARNING,
            implementation = Implementation(
                BlockingCallInRequestPathDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
