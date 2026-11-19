package com.umc.lint

import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
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
        val millis = fixedMillis(argument) ?: return
        if (!isInsideLaunchedEffect(context, node)) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "LaunchedEffect 안에서 고정 시간(${millis}ms)을 기다리면 기기 속도에 따라 " +
                "타이밍이 어긋납니다. 기다리는 대상을 상태로 관찰하세요.",
        )
    }

    /**
     * delay(120), delay(120L), delay(120.milliseconds), delay(3.seconds), 그리고 그런 값을 담은 val을 밀리초로 바꾼다.
     * 고정 값이 아니면 null.
     */
    private fun fixedMillis(argument: UExpression, depth: Int = 0): Long? {
        if (depth > MAX_VAL_HOPS) return null
        constantNumber(argument)?.let { return it.toLong() }

        // Duration은 const가 될 수 없어 이름 붙인 고정 대기는 val로 쓴다. 바뀌지 않는 val이면 초기값을 따라간다.
        val referenced = (argument as? UReferenceExpression)?.resolve()
        val variable = (referenced?.navigationElement.toUElement() ?: referenced.toUElement()) as? UVariable
        if (variable != null) {
            if (variable is UParameter || !variable.isFinal()) return null
            return variable.uastInitializer?.skipParenthesizedExprDown()?.let { fixedMillis(it, depth + 1) }
        }

        val duration = argument as? UQualifiedReferenceExpression ?: return null
        val unit = (duration.selector as? USimpleNameReferenceExpression)?.identifier ?: return null
        val factor = MILLIS_PER_UNIT[unit] ?: return null
        val owner = (duration.resolve() as? PsiMember)?.containingClass?.qualifiedName ?: return null
        if (!owner.startsWith("kotlin.time.Duration")) return null
        val amount = constantNumber(duration.receiver.skipParenthesizedExprDown()) ?: return null
        return (amount.toDouble() * factor).toLong()
    }

    /** 파라미터로 받은 값은 호출부가 정하므로 고정 값으로 보지 않는다. ConstantEvaluator는 파라미터의 기본값까지 읽어 버리므로 먼저 거른다. */
    private fun constantNumber(expression: UExpression): Number? {
        if ((expression as? UReferenceExpression)?.resolve().toUElement() is UParameter) return null
        return ConstantEvaluator.evaluate(null, expression) as? Number
    }

    /** Kotlin 지역 val은 PSI에 final 수식어가 드러나지 않으므로 val/var를 직접 본다. */
    private fun UVariable.isFinal(): Boolean =
        (sourcePsi as? KtProperty)?.isVar?.not() ?: hasModifierProperty(PsiModifier.FINAL)

    /** 동명 함수가 아니라 Compose의 LaunchedEffect 블록 안인지 확인한다. */
    private fun isInsideLaunchedEffect(context: JavaContext, node: UElement): Boolean {
        var cursor: UElement? = node.uastParent
        while (cursor != null && cursor !is UMethod) {
            if (cursor is UCallExpression && context.calls(cursor, "LaunchedEffect", "androidx.compose.runtime")) return true
            cursor = cursor.uastParent
        }
        return false
    }

    companion object {
        /** val이 다른 val을 가리킬 때 따라가는 최대 횟수. */
        private const val MAX_VAL_HOPS = 3

        private val MILLIS_PER_UNIT = mapOf(
            "nanoseconds" to 0.000_001,
            "microseconds" to 0.001,
            "milliseconds" to 1.0,
            "seconds" to 1_000.0,
            "minutes" to 60_000.0,
            "hours" to 3_600_000.0,
            "days" to 86_400_000.0,
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "FixedDelayInLaunchedEffect",
            briefDescription = "LaunchedEffect 안의 고정 시간 대기",
            explanation = """
                LaunchedEffect 안에서 delay(120)처럼 고정 시간을 기다려 키보드 애니메이션이나 레이아웃 \
                안정화가 끝나길 맞추면, 느린 기기·애니메이션 배율 변경·접근성 설정에서 타이밍이 어긋납니다. \
                어긋날 때마다 숫자를 늘리는 땜질로 이어지기 쉽습니다.

                기다리는 대상을 상태로 관찰하세요. snapshotFlow { imeBottom }.first { it > 0 }은 키보드가 \
                나타나기 시작하는 시점에 재개합니다. 키보드가 다 올라온 뒤의 최종 높이가 필요하면, 컴포지션에서 \
                WindowInsets.ime 와 WindowInsets.imeAnimationTarget(ExperimentalLayoutApi) 객체를 얻어 두고 \
                같은 snapshotFlow 안에서 두 객체의 getBottom(density)를 함께 읽어 target > 0 && current == target 일 때 \
                재개하세요(두 getter 는 @Composable 이라 snapshotFlow 블록에서 직접 부를 수 없습니다). 높이 값을 블록 밖에 \
                저장해 두면 이후 변화를 볼 수 없고, 열기가 취소되거나 목표가 바뀔 때 대기가 끝나지 않습니다. \
                열기 요청이 취소되면 대기도 취소하세요. \
                배치가 끝나야 하는 작업(requestFocus() 등)은 대상의 onGloballyPositioned에서 배치 여부를 상태로 \
                기록한 뒤 그 상태가 준비됐을 때 실행하면 됩니다. 자동 닫힘 타이머처럼 시간 자체가 요구사항이라면 \
                @Suppress("FixedDelayInLaunchedEffect")로 의도를 남기세요.
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
