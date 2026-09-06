package com.umc.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

/**
 * 단발성 이벤트를 기본 파라미터 MutableSharedFlow()로 내보내는 것을 금지한다.
 *
 * MutableSharedFlow()의 기본값은 replay=0, extraBufferCapacity=0 이므로 구독자가 0명인 순간의
 * emit 은 아무 로그도 남기지 않고 조용히 버려진다. 화면 구독이 컴포지션 수명을 따르는 Compose
 * 에서는 Activity 재생성(회전·다크모드·글꼴 크기 변경) 구간에 구독자가 0이 되는데, ViewModel은
 * 살아 있으므로 그 사이 발행된 "등록 성공" 같은 결과 이벤트가 사라진다.
 *
 * 실제로 이 프로젝트에서 공지 등록 성공 이벤트가 유실돼 사용자가 재등록 → 공지 2건 생성과
 * 전체 인원 중복 푸시로 이어진 장애가 있었다. 단발성 이벤트는 Channel + receiveAsFlow 를 쓴다.
 */
class OneShotEventFlowDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("MutableSharedFlow")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.containingClass?.qualifiedName?.startsWith("kotlinx.coroutines.flow") != true) return
        // 인자를 명시했다면(replay/extraBufferCapacity 지정) 의도적 설정으로 보고 통과시킨다.
        if (node.valueArgumentCount > 0) return

        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "단발성 이벤트에 기본값 MutableSharedFlow()를 쓰면 구독자가 0인 순간의 " +
                "emit이 유실됩니다. Channel(Channel.BUFFERED) + receiveAsFlow()를 사용하세요.",
        )
    }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "OneShotEventSharedFlow",
            briefDescription = "단발성 이벤트에 기본 MutableSharedFlow 사용",
            explanation = """
                MutableSharedFlow()의 기본값(replay=0, extraBufferCapacity=0)은 구독자가 없을 때 \
                emit된 값을 조용히 버립니다. Compose 화면의 구독은 컴포지션 수명을 따르므로 \
                Activity 재생성 구간에 구독자가 0이 되고, 그 사이 발행된 단발성 이벤트(토스트, \
                화면 이동, 등록 성공)는 사라집니다.

                단발성 이벤트는 Channel(Channel.BUFFERED) + receiveAsFlow()로 내보내세요. \
                버퍼에 보관됐다가 구독이 붙는 시점에 전달되고, 단일 소비라 재구독 시 중복도 없습니다.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 9,
            severity = Severity.ERROR,
            implementation = Implementation(
                OneShotEventFlowDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
