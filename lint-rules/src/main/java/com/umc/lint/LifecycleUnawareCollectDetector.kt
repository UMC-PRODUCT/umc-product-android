package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.skipParenthesizedExprDown

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

        // snapshotFlow 는 컴포지션 상태를 관찰하는 용도라 단발성 이벤트가 아니다. 화면이 가려진 동안에도
        // 스크롤 위치 추적 등은 계속돼야 하므로 제외한다. events.flowWithLifecycle(lifecycle) 은 이미 수명주기를 따른다.
        // 둘 다 collect 로 값이 흘러 들어오는 수신자 체인 전체에 적용될 때만 인정한다.
        // merge(events, other.flowWithLifecycle(lifecycle)) 처럼 인수 하나에만 적용되면 나머지 Flow 는 보호되지 않는다.
        if (isProtectedChain(context, node.receiver)) return

        // 이름이 아니라 해석된 선언(이름+패키지)으로 판정한다. import 별칭은 인정하고 다른 패키지의 동명 함수는 무시한다.
        // repeatOnLifecycle 은 block 으로 넘긴 람다 본문만 보호한다. state 인수를 계산하는 코드는 그 전에 돈다.
        var inLaunchedEffect = false
        var guarded = false
        var cursor: UElement? = node.uastParent
        while (cursor != null) {
            if (cursor is ULambdaExpression) {
                val owner = cursor.ownerCall()
                if (owner != null && context.calls(owner, "repeatOnLifecycle", "androidx.lifecycle") &&
                    isAtLeastStarted(context, owner, "state")
                ) {
                    guarded = true
                }
            }
            if (cursor is UCallExpression && context.calls(cursor, "LaunchedEffect", "androidx.compose.runtime")) {
                inLaunchedEffect = true
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

    /**
     * collect 의 수신자 체인(a.b().c())을 collect 쪽에서부터 거슬러 올라가며 snapshotFlow·flowWithLifecycle 을 찾는다.
     * 그 사이에는 값만 바꾸는 연산자(map, filter, distinctUntilChanged …)만 허용한다. combine·zip·flatMapLatest 처럼
     * 다른 Flow 를 끌어들이는 연산자나 모르는 연산자를 먼저 만나면, 그 Flow 는 보호되지 않으므로 false.
     * 인수 안쪽(merge(x, y.flowWithLifecycle()) 의 y)도 체인이 아니므로 보지 않는다.
     */
    private fun isProtectedChain(context: JavaContext, receiver: UExpression?): Boolean {
        var current = receiver?.skipParenthesizedExprDown()
        while (current != null) {
            val call = when (current) {
                is UQualifiedReferenceExpression -> current.selector as? UCallExpression
                is UCallExpression -> current
                else -> null
            }
            if (call != null) {
                if (STATE_OBSERVERS.any { (name, pkg) -> context.calls(call, name, pkg) }) return true
                if (context.calls(call, "flowWithLifecycle", "androidx.lifecycle")) {
                    // STARTED 이상이면 보호된다. CREATED 처럼 낮으면 값만 흘려보내는 연산자처럼 보고 위쪽 체인을 계속 본다.
                    if (isAtLeastStarted(context, call, "minActiveState")) return true
                } else if (SINGLE_UPSTREAM_OPERATORS.none { context.calls(call, it, "kotlinx.coroutines.flow") }) {
                    return false
                }
            }
            current = when (current) {
                is UQualifiedReferenceExpression -> current.receiver.skipParenthesizedExprDown()
                is UCallExpression -> current.receiver?.skipParenthesizedExprDown()
                else -> null
            }
        }
        return false
    }

    /**
     * 수명주기 상태 인수가 STARTED 이상인지. CREATED 등으로 내리면 화면이 멈춘 뒤에도 수집이 계속되므로 보호로 보지 않는다.
     * 인수를 생략하면 기본값(STARTED)이고, 변수처럼 값을 알 수 없으면 보호로 본다.
     */
    private fun isAtLeastStarted(context: JavaContext, call: UCallExpression, parameterName: String): Boolean {
        val method = call.resolve() ?: return true
        val argument = context.evaluator.computeArgumentMapping(call, method)
            .entries.firstOrNull { it.value.name == parameterName }?.key ?: return true
        val state = ((argument.skipParenthesizedExprDown() as? UReferenceExpression)?.resolve() as? PsiField)?.name
        return state !in BELOW_STARTED
    }

    companion object {
        private val BELOW_STARTED = setOf("DESTROYED", "INITIALIZED", "CREATED")

        private val STATE_OBSERVERS = mapOf(
            "snapshotFlow" to "androidx.compose.runtime",
            "derivedStateOf" to "androidx.compose.runtime",
        )

        /**
         * 업스트림 하나의 값만 바꾸는 연산자. 보호 연산자와 collect 사이에 있어도 보호가 유지된다.
         * onStart·onEmpty·onCompletion·catch·retryWhen·scan·runningFold 는 업스트림을 거치지 않고 값을 직접 내보낼 수 있어 넣지 않는다.
         */
        private val SINGLE_UPSTREAM_OPERATORS = listOf(
            "map", "mapNotNull", "mapLatest", "filter", "filterNot", "filterIsInstance", "filterNotNull",
            "onEach", "distinctUntilChanged", "distinctUntilChangedBy", "take", "takeWhile", "drop", "dropWhile",
            "debounce", "sample", "conflate", "buffer", "runningReduce", "withIndex", "flowOn", "cancellable",
            "retry",
        )

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
