package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement

/**
 * LaunchedEffect 안에서 수명주기를 고려하지 않고 이벤트 Flow를 수집하는 것을 금지한다.
 *
 * LaunchedEffect의 코루틴은 컴포지션이 살아 있는 동안 계속 동작하므로, 앱이 백그라운드로 내려간
 * 뒤에도 수집이 이어진다. 그 결과 보이지 않는 화면에서 토스트가 뜨거나 의도치 않은 화면 이동이
 * 일어난다. repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸 화면이 보이는 동안만 수집한다.
 */
class LifecycleUnawareCollectDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("collect", "collectLatest")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.containingClass?.qualifiedName?.startsWith("kotlinx.coroutines.flow") != true) return

        // snapshotFlow 는 컴포지션 상태를 관찰하는 용도라 단발성 이벤트가 아니다.
        // 화면이 가려진 동안에도 스크롤 위치 추적 등은 계속돼야 하므로 대상에서 제외한다.
        // 소스 텍스트가 아니라 해석된 호출 대상(이름+패키지)을 본다. import 별칭이나 동명 함수에 속지 않는다.
        if (context.containsCallTo(node.receiver, STATE_OBSERVERS)) return
        // events.flowWithLifecycle(lifecycle).collect { } 는 수신자 체인에서 이미 수명주기를 따른다.
        if (context.containsCallTo(node.receiver, LIFECYCLE_AWARE_OPERATORS)) return

        var inLaunchedEffect = false
        var guarded = false
        var cursor: UElement? = node.uastParent
        while (cursor != null) {
            if (cursor is UCallExpression) {
                when (cursor.methodName) {
                    "repeatOnLifecycle", "flowWithLifecycle", "collectAsStateWithLifecycle" -> guarded = true
                    "LaunchedEffect" -> inLaunchedEffect = true
                }
            }
            cursor = cursor.uastParent
        }
        if (!inLaunchedEffect || guarded) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "LaunchedEffect 안의 수집은 백그라운드에서도 계속됩니다. " +
                "repeatOnLifecycle(Lifecycle.State.STARTED)로 감싸세요.",
        )
    }

    companion object {
        private val STATE_OBSERVERS = mapOf(
            "snapshotFlow" to "androidx.compose.runtime",
            "derivedStateOf" to "androidx.compose.runtime",
        )
        private val LIFECYCLE_AWARE_OPERATORS = mapOf("flowWithLifecycle" to "androidx.lifecycle")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "LifecycleUnawareEventCollect",
            briefDescription = "수명주기를 고려하지 않은 이벤트 수집",
            explanation = """
                LaunchedEffect의 코루틴은 컴포지션이 살아 있는 한 계속 실행되므로 앱이 백그라운드로 \
                내려가도 Flow 수집이 이어집니다. 그 사이 도착한 단발성 이벤트가 소비되면 보이지 않는 \
                화면에서 토스트가 뜨거나 의도치 않은 네비게이션이 발생합니다.

                repeatOnLifecycle(Lifecycle.State.STARTED) 안에서 수집해 화면이 보이는 동안만 \
                소비하도록 하세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                LifecycleUnawareCollectDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
