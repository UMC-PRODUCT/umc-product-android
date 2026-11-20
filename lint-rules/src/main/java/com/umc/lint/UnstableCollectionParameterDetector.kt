package com.umc.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UMethod

/**
 * @Composable 함수가 List·Set·Map 을 그대로 파라미터로 받는 것을 경고한다.
 *
 * Kotlin 2.x 의 strong skipping 에서는 불안정한 파라미터도 건너뛸 수 있지만, 비교가 equals 가 아니라 인스턴스(===)로 바뀐다.
 * 그래서 컴포지션 안에서 filter·map 으로 내용이 같은 새 리스트를 만들어 넘기면 매번 다시 그린다.
 * kotlinx.collections.immutable 의 ImmutableList 등은 compose_stability_config.conf 에 stable 로 등록돼 있어 equals 로 비교된다.
 */
class UnstableCollectionParameterDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitMethod(node: UMethod) {
            val annotations = node.uAnnotations.mapNotNull { it.qualifiedName }
            if (COMPOSABLE !in annotations) return
            if (node.isPreview()) return

            node.uastParameters.forEach { parameter ->
                val type = parameter.type as? PsiClassType ?: return@forEach
                val cls = type.resolve() ?: return@forEach
                if (!isMutableOrPlainCollection(cls)) return@forEach
                val element: UElement = parameter
                context.report(
                    issue = ISSUE,
                    scope = element,
                    location = context.getLocation(element),
                    message = "${parameter.name}: ${typeText(parameter, type)} 는 내용이 같아도 새 인스턴스면 다시 그립니다. " +
                        "ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 받으세요.",
                )
            }
        }
    }

    /** 소스에 적힌 타입 이름(MutableList<String> 등). Java 에서 온 파라미터면 클래스 이름. */
    private fun typeText(parameter: UParameter, type: PsiClassType): String =
        (parameter.sourcePsi as? KtParameter)?.typeReference?.text ?: type.className

    /** java.util.Collection·Map 계열이면서 kotlinx.collections.immutable 이나 @Stable·@Immutable 타입이 아닌지. */
    private fun isMutableOrPlainCollection(cls: PsiClass): Boolean {
        val name = cls.qualifiedName ?: return false
        if (name.startsWith(IMMUTABLE_PACKAGE)) return false
        if (cls.annotations.any { it.qualifiedName in STABILITY_ANNOTATIONS }) return false
        return COLLECTION_ROOTS.any { root -> name == root || InheritanceUtil.isInheritor(cls, root) }
    }

    companion object {
        private const val COMPOSABLE = "androidx.compose.runtime.Composable"
        private const val IMMUTABLE_PACKAGE = "kotlinx.collections.immutable."
        private val STABILITY_ANNOTATIONS = setOf("androidx.compose.runtime.Stable", "androidx.compose.runtime.Immutable")
        private val COLLECTION_ROOTS = listOf("java.util.Collection", "java.util.Map")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "UnstableCollectionParameter",
            briefDescription = "Composable 파라미터에 일반 컬렉션 사용",
            explanation = """
                List·Set·Map 은 Compose 컴파일러가 안정적인 타입으로 보지 않습니다. Kotlin 2.x 의 strong skipping 에서는 \
                이런 파라미터도 건너뛸 수 있지만 비교가 equals 가 아니라 인스턴스 비교(===)가 되어, 컴포지션 안에서 \
                filter·map 등으로 내용이 같은 새 리스트를 만들어 넘기면 매번 다시 그립니다.

                UiState 에서 ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 만들어 넘기세요. \
                toImmutableList()·persistentListOf()를 쓰면 됩니다. 이 타입들은 compose_stability_config.conf 에 stable 로 \
                등록돼 있어 equals 로 비교됩니다.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 4,
            severity = Severity.WARNING,
            implementation = Implementation(UnstableCollectionParameterDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
    }
}
