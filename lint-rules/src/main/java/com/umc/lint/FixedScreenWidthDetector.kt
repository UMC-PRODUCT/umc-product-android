package com.umc.lint

import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.toUElement

/**
 * 화면 폭을 dp로 못 박는 것과, fillMaxWidth 뒤에 둬서 효과가 없는 widthIn 을 찾는다.
 *
 * - width(350.dp)처럼 화면에 가까운 폭을 고정하면 좁은 폰(320dp)·분할 화면·접힌 폴더블에서 좌우 여백이 사라진다.
 *   메뉴·말풍선·QR 처럼 콘텐츠 크기로 쓰는 300dp 미만의 폭은 정상이라 잡지 않는다.
 * - fillMaxWidth() 가 폭을 들어온 최대값으로 고정한 뒤에는 widthIn(max = …)이 그보다 줄일 수 없어 상한이 무시된다.
 */
class FixedScreenWidthDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = WIDTH_PARAMETERS.keys.toList()

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.getPackage(method)?.qualifiedName != LAYOUT_PACKAGE) return
        val name = sourceName(method.name)
        val widthParameters = WIDTH_PARAMETERS[name] ?: return
        if (isInsidePreview(node)) return

        val mapping = context.evaluator.computeArgumentMapping(node, method)
        // 높이 인자만 준 sizeIn(minHeight = …)은 fillMaxWidth 뒤에서도 적용되므로 폭 인자가 있을 때만 순서를 본다.
        if (name in LIMIT_FUNCTIONS && mapping.values.any { it.name in LIMIT_WIDTH_PARAMETERS }) {
            reportIfAfterFillMaxWidth(context, node, name)
        }

        val fixed = mapping.entries
            .filter { (_, parameter) -> parameter.name in widthParameters }
            .firstNotNullOfOrNull { (argument, _) -> fixedDp(context, argument.skipParenthesizedExprDown())?.takeIf { it >= THRESHOLD_DP } }
            ?: return
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "${fixed.toInt()}dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. " +
                "padding(horizontal = …).widthIn(max = ${fixed.toInt()}.dp).fillMaxWidth() 처럼 상한으로 바꾸세요.",
        )
    }

    /**
     * widthIn·sizeIn 이 같은 modifier 체인에서 fillMaxWidth·fillMaxSize 뒤에 오면 보고한다.
     * 체인을 거슬러 올라가다 wrapContent*·required* 처럼 제약을 다시 정하는 modifier 를 만나면, 그 뒤의 widthIn 은
     * 적용되므로 멈춘다. Modifier.fillMaxWidth().then(Modifier.widthIn(…)) 처럼 then 안에 있거나, 지역 val 로
     * 나눈 체인(val base = Modifier.fillMaxWidth(); base.widthIn(…))도 이어서 본다.
     */
    private fun reportIfAfterFillMaxWidth(context: JavaContext, node: UCallExpression, name: String) {
        var receiver: UExpression? = chainReceiver(node)
        var hops = 0
        while (receiver != null && hops <= MAX_CHAIN_HOPS) {
            val call = when (receiver) {
                is UQualifiedReferenceExpression -> receiver.selector as? UCallExpression
                is UCallExpression -> receiver
                else -> null
            }
            if (call != null) {
                if (RESET_FUNCTIONS.any { context.calls(call, it, LAYOUT_PACKAGE) }) return
                if (FILL_FUNCTIONS.any { context.calls(call, it, LAYOUT_PACKAGE) }) {
                    context.report(
                        issue = ORDER_ISSUE,
                        scope = node,
                        location = context.getLocation(node),
                        message = "${sourceName(call.methodName ?: "")}() 가 폭을 먼저 고정해 $name 의 상한이 적용되지 않습니다. " +
                            "$name 을 ${sourceName(call.methodName ?: "")}() 앞에 두세요.",
                    )
                    return
                }
            }
            receiver = when (receiver) {
                is UQualifiedReferenceExpression -> receiver.receiver.skipParenthesizedExprDown()
                is UCallExpression -> receiver.receiver?.skipParenthesizedExprDown()
                is USimpleNameReferenceExpression -> {
                    hops++
                    localValInitializer(receiver) ?: chainReceiver(receiver)
                }
                else -> null
            }
        }
    }

    /**
     * [node] 의 수신자. 체인이 Modifier(동반 객체)에서 시작하고 그 체인이 then(…)의 인자라면, then 을 부른 쪽 체인으로 잇는다.
     */
    private fun chainReceiver(node: UElement): UExpression? {
        val own = (node as? UCallExpression)?.receiver?.skipParenthesizedExprDown()
        if (own != null && !(own is USimpleNameReferenceExpression && own.identifier == "Modifier")) return own
        var outer: UElement? = node.uastParent
        while (outer is UQualifiedReferenceExpression || outer is UParenthesizedExpression) outer = outer.uastParent
        val thenCall = outer as? UCallExpression ?: return null
        if (thenCall.methodName != "then") return null
        return thenCall.receiver?.skipParenthesizedExprDown()
    }

    /** 다시 대입할 수 없는 지역 val 의 초기화식. */
    private fun localValInitializer(reference: USimpleNameReferenceExpression): UExpression? {
        val variable = reference.resolve().toUElement() as? UVariable ?: return null
        if (variable is UParameter || !variable.isFinal()) return null
        return variable.uastInitializer?.skipParenthesizedExprDown()
    }

    /** 350.dp, 350.0.dp, Dp(350f), 그리고 그런 값을 담은 바뀌지 않는 val 을 dp 숫자로 바꾼다. 고정 값이 아니면 null. */
    private fun fixedDp(context: JavaContext, expression: UExpression, depth: Int = 0): Double? {
        if (depth > MAX_VAL_HOPS) return null
        if (expression is UQualifiedReferenceExpression) {
            val selector = expression.selector
            // 350.dp 는 Int.dp 확장 프로퍼티다.
            if (selector is USimpleNameReferenceExpression && selector.identifier == "dp" && isUnitApi(expression.resolve())) {
                return constantNumber(expression.receiver.skipParenthesizedExprDown())
            }
            // androidx.compose.ui.unit.Dp(350f) 처럼 패키지를 붙여 부른 생성자.
            if (selector is UCallExpression) return dpConstructorValue(context, selector)
        }
        if (expression is UCallExpression) return dpConstructorValue(context, expression)
        // 이름 붙인 고정 폭은 val 로 쓴다. 바뀌지 않는 val 이면 초기값을 따라간다(패키지를 붙여 불러도 같다).
        val referenced = (expression as? UReferenceExpression)?.resolve() ?: return null
        val variable = (referenced.navigationElement.toUElement() ?: referenced.toUElement()) as? UVariable ?: return null
        if (variable is UParameter || !variable.isFinal()) return null
        return variable.uastInitializer?.skipParenthesizedExprDown()?.let { fixedDp(context, it, depth + 1) }
    }

    /** Dp(350f) 생성자나 DpSize(350.dp, 48.dp) 호출이면 폭 숫자. */
    private fun dpConstructorValue(context: JavaContext, call: UCallExpression): Double? {
        if (sourceName(call.methodName ?: "") == "DpSize") {
            val method = call.resolve()?.takeIf(::isUnitApi) ?: return null
            // DpSize(height = …, width = …)처럼 이름 붙여 순서를 바꿔도 폭 인자를 고른다.
            val width = context.evaluator.computeArgumentMapping(call, method).entries.firstOrNull { it.value.name == "width" }?.key
            return width?.let { fixedDp(context, it.skipParenthesizedExprDown()) }
        }
        val constructor = call.resolve()
        val isDp = constructor?.containingClass?.qualifiedName == "$UNIT_PACKAGE.Dp" ||
            (constructor == null && call.methodName == "Dp")
        if (!isDp) return null
        return call.valueArguments.firstOrNull()?.let { constantNumber(it.skipParenthesizedExprDown()) }
    }

    private fun isUnitApi(element: PsiElement?): Boolean {
        val member = element as? PsiMember ?: return false
        val owner = member.containingClass?.qualifiedName ?: return false
        return owner.startsWith("$UNIT_PACKAGE.")
    }

    /** 파라미터로 받은 값은 호출부가 정하므로 고정 값으로 보지 않는다. */
    private fun constantNumber(expression: UExpression): Double? {
        if ((expression as? UReferenceExpression)?.resolve().toUElement() is UParameter) return null
        return (ConstantEvaluator.evaluate(null, expression) as? Number)?.toDouble()
    }

    private fun UVariable.isFinal(): Boolean =
        (sourcePsi as? KtProperty)?.isVar?.not() ?: hasModifierProperty(PsiModifier.FINAL)

    /** @Preview 함수는 미리보기 창 크기를 정하려고 폭을 고정하는 경우가 있어 제외한다. */
    private fun isInsidePreview(node: UElement): Boolean =
        node.getParentOfType(UMethod::class.java, true)?.isPreview() == true

    companion object {
        /** 가장 좁은 폰(320dp)에서 여백이 20dp 이하로 남는 폭부터 화면 폭을 가정한 것으로 본다. */
        private const val THRESHOLD_DP = 300.0
        private const val MAX_VAL_HOPS = 3
        private const val LAYOUT_PACKAGE = "androidx.compose.foundation.layout"
        private const val UNIT_PACKAGE = "androidx.compose.ui.unit"
        private const val MAX_CHAIN_HOPS = 3

        /** 폭을 정하는 modifier 와 그 폭 파라미터. 상한(max, maxWidth)은 고정 폭이 아니므로 넣지 않는다. */
        private val WIDTH_PARAMETERS = mapOf(
            "width" to setOf("width"),
            "requiredWidth" to setOf("width"),
            "size" to setOf("size", "width"),
            "requiredSize" to setOf("size", "width"),
            "widthIn" to setOf("min"),
            "requiredWidthIn" to setOf("min"),
            "sizeIn" to setOf("minWidth"),
            "requiredSizeIn" to setOf("minWidth"),
            "defaultMinSize" to setOf("minWidth"),
        )

        /** 들어온 제약 안에서만 폭을 줄이는 modifier. required* 는 들어온 제약을 무시하므로 순서 검사에서 뺀다. */
        private val LIMIT_FUNCTIONS = setOf("widthIn", "sizeIn")
        private val LIMIT_WIDTH_PARAMETERS = setOf("min", "max", "minWidth", "maxWidth")

        /** 뒤에 오는 modifier 에 넘기는 폭 제약을 다시 정해, 그 뒤의 widthIn 이 적용되게 하는 modifier. */
        private val RESET_FUNCTIONS = listOf(
            "wrapContentWidth", "wrapContentSize", "requiredWidth", "requiredWidthIn", "requiredSize", "requiredSizeIn",
        )
        private val FILL_FUNCTIONS = listOf("fillMaxWidth", "fillMaxSize")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "FixedScreenWidth",
            briefDescription = "화면 폭에 가까운 고정 폭",
            explanation = """
                width(350.dp)처럼 300dp 이상을 고정하면, 그 폭은 화면에 가까운 폭을 가정한 것입니다. width 는 부모보다 \
                넓어지지 않으므로 넘치지는 않지만, 320dp 폰이나 분할 화면·접힌 폴더블에서는 좌우 여백이 0에 가깝게 \
                사라집니다. 레이아웃은 기기 종류가 아니라 창 크기를 기준으로 만듭니다(CLAUDE.md).

                padding(horizontal = 20.dp).widthIn(max = 350.dp).fillMaxWidth() 처럼 여백을 먼저 두고 상한을 거세요. \
                widthIn 은 fillMaxWidth 앞에 둬야 상한이 적용됩니다. 메뉴·말풍선·QR 처럼 콘텐츠 크기라 300dp 이상이 \
                꼭 필요하다면 @Suppress("FixedScreenWidth")로 의도를 남기세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(FixedScreenWidthDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )

        @JvmField
        val ORDER_ISSUE: Issue = Issue.create(
            id = "WidthInAfterFillMaxWidth",
            briefDescription = "fillMaxWidth 뒤에 있어 적용되지 않는 widthIn",
            explanation = """
                modifier 는 앞에서부터 제약을 정합니다. fillMaxWidth() 가 폭을 들어온 최대값으로 고정한 뒤에는 \
                widthIn(max = …)·sizeIn(maxWidth = …)이 그보다 줄일 수 없어, 태블릿처럼 넓은 창에서도 상한이 무시됩니다.

                widthIn 을 fillMaxWidth 앞에 두세요. 예: padding(horizontal = 24.dp).widthIn(max = 370.dp).fillMaxWidth()
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(FixedScreenWidthDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
