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
 * :presentation:component 밖에서 Compose 기본 입력창(TextField, BasicTextField 등)을 직접 쓰는 것을 금지한다.
 *
 * 입력 동작(IME 액션에 따른 한 줄 입력, 비밀번호 마스킹, 입력 거절, 포커스 색상)은 UTextField 한 곳에 모여 있다.
 * 화면마다 입력창을 직접 만들면 이 동작이 화면별로 갈라지고, 입력 정책을 바꿀 때 고칠 곳이 흩어진다.
 */
class UseUTextFieldDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = TARGETS.keys.toList()

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.isOneOf(method, TARGETS)) return
        if (context.isInComponentPackage()) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getNameLocation(node),
            message = "${sourceName(method.name)}를 화면에서 직접 쓰면 입력 동작이 화면마다 갈라집니다. " +
                "com.umc.component.component.UTextField를 사용하세요.",
        )
    }

    companion object {
        private val MATERIAL = setOf("androidx.compose.material3", "androidx.compose.material")
        private val FOUNDATION = setOf("androidx.compose.foundation.text")
        private val TARGETS = mapOf(
            "TextField" to MATERIAL,
            "OutlinedTextField" to MATERIAL,
            "BasicTextField" to FOUNDATION,
            "BasicSecureTextField" to FOUNDATION,
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "UseUTextField",
            briefDescription = "UTextField 대신 Compose 기본 입력창 사용",
            explanation = """
                UTextField(com.umc.component.component.UTextField)는 IME 액션에 따른 한 줄 입력 전환, \
                비밀번호 마스킹, 입력 거절(acceptInput/onInputRejected), 포커스 색상 같은 입력 동작을 \
                한곳에서 처리합니다. 화면에서 TextField나 BasicTextField를 직접 쓰면 이 동작이 화면마다 \
                달라지고, 입력 정책을 바꿀 때 고칠 곳이 흩어집니다.

                UTextField를 사용하세요. 채팅 입력창처럼 UTextField로 표현할 수 없는 모양이 필요하면 \
                호출부에서 BasicTextField를 쓰지 말고 :presentation:component에 파라미터나 새 공용 \
                컴포넌트를 추가하세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                UseUTextFieldDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
