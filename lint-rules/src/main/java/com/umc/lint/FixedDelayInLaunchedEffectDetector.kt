package com.umc.lint

import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.toUElement

/**
 * LaunchedEffect 안에서 고정 시간 delay로 다른 동작이 끝나길 기다리는 것을 경고한다.
 *
 * 키보드 애니메이션이나 레이아웃 안정화를 "120ms면 끝나겠지"로 맞추면 느린 기기, 애니메이션 배율 변경,
 * 접근성 설정에서 타이밍이 어긋난다. 어긋나면 숫자를 늘리는 땜질로 이어지기 쉬우므로, 기다리는 대상을
 * 상태로 관찰하게 한다. 자동 닫힘 타이머처럼 시간 자체가 요구사항인 경우가 있어 경고로 둔다.
 */
class FixedDelayInLaunchedEffectDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("delay")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.getPackage(method)?.qualifiedName != "kotlinx.coroutines") return
        val argument = node.valueArguments.firstOrNull()?.skipParenthesizedExprDown() ?: return
        // 파라미터로 받은 시간은 호출부가 정하는 값이다. 기본값이 있어도 ConstantEvaluator가 그 기본값을 읽어 버리므로 먼저 거른다.
        if ((argument as? UReferenceExpression)?.resolve().toUElement() is UParameter) return
        val millis = ConstantEvaluator.evaluate(null, argument) as? Number ?: return
        if (!isInsideLaunchedEffect(node)) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "LaunchedEffect 안에서 고정 시간(${millis.toLong()}ms)을 기다리면 기기 속도에 따라 " +
                "타이밍이 어긋납니다. 기다리는 대상을 상태로 관찰하세요.",
        )
    }

    private fun isInsideLaunchedEffect(node: UElement): Boolean {
        var cursor: UElement? = node.uastParent
        while (cursor != null && cursor !is UMethod) {
            if (cursor is UCallExpression && cursor.methodName == "LaunchedEffect") return true
            cursor = cursor.uastParent
        }
        return false
    }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "FixedDelayInLaunchedEffect",
            briefDescription = "LaunchedEffect 안의 고정 시간 대기",
            explanation = """
                LaunchedEffect 안에서 delay(120)처럼 고정 시간을 기다려 키보드 애니메이션이나 레이아웃 \
                안정화가 끝나길 맞추면, 느린 기기·애니메이션 배율 변경·접근성 설정에서 타이밍이 어긋납니다. \
                어긋날 때마다 숫자를 늘리는 땜질로 이어지기 쉽습니다.

                기다리는 대상을 상태로 관찰하세요. 키보드는 snapshotFlow { imeBottom }.first { it > 0 }, \
                레이아웃 반영은 withFrameNanos { }, 포커스는 화면이 배치된 뒤 requestFocus()처럼 \
                조건이 실제로 충족되는 시점을 기다리면 됩니다. 자동 닫힘 타이머처럼 시간 자체가 \
                요구사항이라면 @Suppress("FixedDelayInLaunchedEffect")로 의도를 남기세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                FixedDelayInLaunchedEffectDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
