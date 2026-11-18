package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

/**
 * :presentation:component 밖에서 Compose 기본 텍스트(material3 Text 등)를 직접 쓰는 것을 금지한다.
 *
 * UText는 fontScale을 1로 고정해 시스템 글자 크기와 관계없이 디자인 스펙의 크기를 유지한다.
 * 기본 Text를 섞어 쓰면 글자 크기를 키운 기기에서 그 부분만 커져 줄바꿈·잘림이 생긴다.
 */
class UseUTextDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = TARGETS.keys.toList()

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.isOneOf(method, TARGETS)) return
        if (context.isInComponentPackage()) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getNameLocation(node),
            message = "${sourceName(method.name)}는 시스템 글자 크기를 그대로 따라가 레이아웃이 깨질 수 있습니다. " +
                "com.umc.component.component.UText를 사용하세요.",
        )
    }

    companion object {
        private val TARGETS = mapOf(
            "Text" to setOf("androidx.compose.material3", "androidx.compose.material"),
            "BasicText" to setOf("androidx.compose.foundation.text"),
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "UseUText",
            briefDescription = "UText 대신 Compose 기본 Text 사용",
            explanation = """
                UText(com.umc.component.component.UText)는 fontScale을 1로 고정해 시스템 글자 크기 \
                설정과 관계없이 디자인 스펙의 크기를 유지합니다. material3 Text를 직접 쓰면 글자 크기를 \
                키운 기기에서 그 텍스트만 커져 줄바꿈이나 잘림이 생기고, 같은 화면 안에서도 크기가 \
                들쭉날쭉해집니다.

                Text를 UText로 바꾸세요. text, modifier, color, style, maxLines, overflow, textAlign 등 \
                자주 쓰는 파라미터는 같습니다. UText에 필요한 파라미터가 없다면 호출부에서 Text로 \
                우회하지 말고 :presentation:component의 UText에 추가하세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                UseUTextDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
