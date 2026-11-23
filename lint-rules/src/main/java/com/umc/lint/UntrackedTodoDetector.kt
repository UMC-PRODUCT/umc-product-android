package com.umc.lint

import com.android.resources.ResourceFolderType
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.util.EnumSet

/**
 * 추적 이슈 번호가 없는 땜질 표시(TODO·FIXME·[임시])를 금지한다.
 *
 * 번호 없는 TODO 는 누가 언제 처리할지 아무도 모른 채 남는다. 이 저장소에서는 TODO 21건이 모두 번호 없이 쌓였고,
 * 명함 교환 차단처럼 여러 파일에 흩어진 [임시] 처리는 되돌릴 때 빠뜨리기 쉬웠다.
 * Kotlin·Java 주석과 리소스·매니페스트 XML 주석을 본다.
 */
class UntrackedTodoDetector : Detector(), SourceCodeScanner, XmlScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UFile::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitFile(node: UFile) {
            // UFile.allCommentsInFile 은 Kotlin 의 KDoc 을 빼고 돌려주므로 PSI 에서 주석을 직접 모은다(KDoc 도 PsiComment 다).
            val file = node.sourcePsi
            for (comment in PsiTreeUtil.collectElementsOfType(file, PsiComment::class.java)) {
                for (marker in untrackedMarkers(comment.text)) {
                    context.report(
                        issue = ISSUE,
                        scope = comment,
                        location = context.getRangeLocation(comment, marker.range.first, marker.value.length),
                        message = message(marker.value),
                    )
                }
            }
        }
    }

    override fun appliesTo(folderType: ResourceFolderType): Boolean = true

    override fun visitDocument(context: XmlContext, document: Document) {
        forEachComment(document) { comment ->
            for (marker in untrackedMarkers(comment.nodeValue.orEmpty())) {
                context.report(
                    issue = ISSUE,
                    scope = comment,
                    location = context.getLocation(comment),
                    message = message(marker.value),
                )
            }
        }
    }

    private fun forEachComment(node: Node, action: (Node) -> Unit) {
        if (node.nodeType == Node.COMMENT_NODE) action(node)
        var child = node.firstChild
        while (child != null) {
            forEachComment(child, action)
            child = child.nextSibling
        }
    }

    private fun untrackedMarkers(text: String): Sequence<MatchResult> = MARKER.findAll(text)

    private fun message(marker: String): String =
        if (marker == TEMPORARY) {
            "[임시] 표시는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호) 형식으로 바꾸세요."
        } else {
            "이슈 번호 없는 $marker 는 추적되지 않습니다. 추적 이슈를 만들고 $marker(#이슈번호)처럼 번호를 붙이세요."
        }

    companion object {
        private const val TEMPORARY = "[임시]"

        /** 영숫자에 붙지 않은 TODO·FIXME 중 바로 뒤에 `(#번호)`가 없는 것, 그리고 모든 `[임시]`. */
        private val MARKER = Regex("""(?<![A-Za-z0-9_])(?:TODO|FIXME)(?![A-Za-z0-9_])(?!\(#\d+\))|\[임시]""")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "UntrackedTodo",
            briefDescription = "추적 이슈 번호 없는 TODO·FIXME·[임시]",
            explanation = """
                땜질 표시는 처리할 이슈와 연결돼야 추적됩니다. 번호 없는 TODO·FIXME 와 [임시] 주석은 \
                누가 언제 처리할지 정해지지 않은 채 남고, 여러 곳에 흩어진 임시 처리는 되돌릴 때 일부를 빠뜨리기 쉽습니다.

                GitHub 이슈를 만들고 `TODO(#123)` 또는 `FIXME(#123)` 형식으로 번호를 붙이세요. \
                이미 끝난 일이거나 설명일 뿐이라면 표시를 지우고 이유를 설명하는 주석으로 바꾸세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 4,
            severity = Severity.ERROR,
            implementation = Implementation(
                UntrackedTodoDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.RESOURCE_FILE, Scope.MANIFEST),
                Scope.JAVA_FILE_SCOPE,
                Scope.RESOURCE_FILE_SCOPE,
                Scope.MANIFEST_SCOPE,
            ),
        )
    }
}
