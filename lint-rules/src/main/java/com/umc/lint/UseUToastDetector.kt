package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

/**
 * 시스템 토스트(android.widget.Toast)를 금지한다.
 *
 * 시스템 토스트는 디자인 시스템과 모양·위치가 달라 한 화면 안에서 두 가지 토스트가 섞인다.
 * 화면에서는 앱 공용 토스트(LocalUToast)를, 바텀시트·다이얼로그 안에서는 그 안에 둔 UToastHost 를 쓴다.
 */
class UseUToastDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("makeText")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, TOAST_CLASS)) return
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getNameLocation(node),
            message = "시스템 Toast 대신 UToast를 쓰세요. 화면에서는 LocalUToast.current.show(…), " +
                "바텀시트·다이얼로그 안에서는 그 안에 둔 UToastHost로 띄웁니다.",
        )
    }

    companion object {
        private const val TOAST_CLASS = "android.widget.Toast"

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "UseUToast",
            briefDescription = "UToast 대신 시스템 Toast 사용",
            explanation = """
                시스템 Toast 는 디자인 시스템의 UToast 와 모양·위치가 달라, 같은 화면에서 두 가지 토스트가 섞입니다. \
                ViewModel 이나 Context 를 들고 있는 코드에서 띄우기 쉬워 UI 로직이 화면 밖으로 새기도 합니다.

                화면에서는 앱 공용 토스트를 쓰세요: val toast = LocalUToast.current; toast.show(message, UToastState.ERROR). \
                화면을 떠나도 남으므로 저장 직후 뒤로 가는 경우에도 보입니다. 바텀시트·다이얼로그는 별도 창이라 \
                앱 토스트가 그 아래에 가려지므로, 그 안에 UToastHost 를 두고 띄우세요. ViewModel 에서는 이벤트만 보내세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(UseUToastDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
