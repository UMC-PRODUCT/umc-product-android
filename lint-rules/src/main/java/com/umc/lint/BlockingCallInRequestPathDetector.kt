package com.umc.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.toUElementOfType
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * 네트워크 요청 경로(인터셉터·인증자·요청 빌더)에서 runBlocking 사용을 금지한다.
 *
 * OkHttp Interceptor / Authenticator, Ktor 의 defaultRequest 블록은 요청을 시작한 스레드에서
 * 실행된다. ViewModel 이 대부분 메인 디스패처에서 UseCase 를 호출하므로, 이 경로의 runBlocking
 * 은 모든 API 호출마다 메인 스레드를 디스크 I/O 시간만큼 정지시킨다.
 *
 * 토큰 적재는 suspend 경로(Ktor Auth 의 loadTokens, OkHttp 라면 미리 캐시한 값)로 옮긴다.
 */
class BlockingCallInRequestPathDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("runBlocking")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.containingClass?.qualifiedName?.startsWith("kotlinx.coroutines") != true) return
        if (!RequestPathSearch(context).isInRequestCallback(node, depth = 0)) return

        val fileName = context.file.name
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "요청 경로($fileName)의 runBlocking은 API 호출마다 메인 스레드를 정지시킵니다. " +
                "토큰 적재를 suspend 경로로 옮기세요.",
        )
    }

    /**
     * runBlocking 하나가 요청마다 실행되는 코드 안인지 찾는다. 파일 이름이 아니라 실제 콜백을 본다.
     * - Interceptor·Authenticator 의 intercept·authenticate 를 실제로 재정의한 메서드 본문
     * - Interceptor { … }, addInterceptor { … }, Ktor defaultRequest { … } 처럼 콜백으로 넘긴 람다의 본문
     * - 위 콜백이 쓰는 보조 코드(여러 단계도 따라간다): 함수, 로컬 함수, 커스텀 getter, 람다를 담은 프로퍼티·변수,
     *   기반 클래스의 콜백이 부르는 훅을 재정의한 메서드. 호출 위치는 현재 파일과 선언이 있는 파일에서 찾는다.
     * 프로퍼티 초기화식·init 블록, 콜백에 넘길 인수를 계산하는 코드, NetworkModule의 앱 시작 초기화처럼
     * 한 번만 도는 코드는 해당하지 않는다.
     */
    private class RequestPathSearch(private val context: JavaContext) {
        /** 지금 따라가는 경로에 있는 선언. 순환만 막고, 다른 경로에서 같은 선언을 더 짧게 만나면 다시 본다. */
        private val visiting = mutableSetOf<PsiElement>()

        /** 이미 false 로 끝난 선언 묶음과 그때의 깊이. 같거나 더 깊은 곳에서 다시 만나면 건너뛴다. */
        private val failedAtDepth = mutableMapOf<Set<PsiElement>, Int>()

        fun isInRequestCallback(node: UElement, depth: Int): Boolean {
            var cursor: UElement? = node.uastParent
            while (cursor != null) {
                when (cursor) {
                    is ULambdaExpression -> {
                        if (isCallbackLambda(cursor)) return true
                        // 로컬 함수(fun 안의 fun)와 람다를 담은 변수·프로퍼티는 이름이 불릴 때마다 돈다.
                        val holder = (cursor.uastParent as? UVariable)?.sourcePsi as? KtNamedDeclaration
                        if (holder != null) return isUsedFromRequestCallback(setOf(holder), depth, calledByName = holder)
                    }
                    is UMethod ->
                        return isRequestCallbackMethod(cursor) ||
                            isUsedFromRequestCallback(declarationsOf(cursor), depth, calledByName = null)
                    // 메서드 밖(프로퍼티 초기화식·init 블록)은 인스턴스를 만들 때 한 번 돈다.
                    is UClass -> return false
                }
                cursor = cursor.uastParent
            }
            return false
        }

        private fun isCallbackLambda(lambda: ULambdaExpression): Boolean {
            val type = (lambda.functionalInterfaceType as? PsiClassType)?.resolve()?.qualifiedName
            if (type in REQUEST_CALLBACK_TYPES) return true
            // OkHttp 의 addInterceptor { chain -> } 는 SAM 이 아니라 Kotlin 함수 타입을 받는다.
            // 람다가 그 호출의 인수로 바로 넘어갈 때만 콜백이다. 인수를 계산하는 코드는 등록할 때 한 번 돈다.
            val owner = lambda.ownerCall() ?: return false
            return OKHTTP_BUILDER_CALLBACKS.any { context.calls(owner, it, "okhttp3") } ||
                context.calls(owner, "defaultRequest", "io.ktor.client.plugins")
        }

        /** 이름이 같은 오버로드가 아니라 Interceptor.intercept·Authenticator.authenticate 를 실제로 재정의한 메서드인지. */
        private fun isRequestCallbackMethod(method: UMethod): Boolean {
            if (method.name !in REQUEST_CALLBACK_METHODS) return false
            // findDeepestSuperMethods 는 lint 환경에 검색 확장이 없어 쓸 수 없으므로 상위 메서드를 직접 거슬러 올라간다.
            val pending = ArrayDeque(method.javaPsi.findSuperMethods().toList())
            val seen = mutableSetOf<PsiMethod>()
            while (pending.isNotEmpty()) {
                val superMethod = pending.removeFirst()
                if (!seen.add(superMethod)) continue
                if (superMethod.containingClass?.qualifiedName in REQUEST_CALLBACK_TYPES) return true
                pending.addAll(superMethod.findSuperMethods())
            }
            return false
        }

        /** 메서드를 가리킬 수 있는 선언들: 자기 자신, getter 라면 그 프로퍼티, 재정의했다면 상위 선언. */
        private fun declarationsOf(method: UMethod): Set<PsiElement> {
            val declarations = mutableSetOf<PsiElement>()
            method.sourcePsi?.let { source ->
                declarations += source
                (source as? KtPropertyAccessor)?.property?.let { declarations += it }
            }
            method.javaPsi.findSuperMethods().forEach { declarations += it.navigationElement }
            return declarations
        }

        /**
         * [declarations] 를 부르거나 읽는 곳 중 하나라도 요청 콜백 안이면 true.
         * 호출 위치는 현재 파일과, 선언이 들어 있는 소스 파일(기반 클래스가 다른 파일에 있는 경우)에서 찾는다.
         *
         * [calledByName] 은 로컬 함수나 람다를 담은 변수·프로퍼티다. 이런 호출은 UAST 가 선언으로 해석하지 못하므로
         * (로컬 함수는 가짜 메서드로, tokenProvider() 는 Function0.invoke 로 해석된다) 선언이 보이는 범위
         * (감싸는 함수 본문이나 클래스 본문) 안에서 같은 이름을 부르는 호출을 찾는다.
         */
        private fun isUsedFromRequestCallback(
            declarations: Set<PsiElement>,
            depth: Int,
            calledByName: KtNamedDeclaration?,
        ): Boolean {
            if (declarations.isEmpty() || depth >= MAX_HELPER_DEPTH || declarations.any { it in visiting }) return false
            if ((failedAtDepth[declarations] ?: Int.MAX_VALUE) <= depth) return false
            visiting += declarations
            try {
                val found = filesToScan(declarations).any { file -> scan(file, declarations, depth, calledByName) }
                if (!found) failedAtDepth[declarations] = depth
                return found
            } finally {
                visiting -= declarations
            }
        }

        private fun filesToScan(declarations: Set<PsiElement>): List<UFile> {
            val current = context.uastFile
            // 라이브러리의 컴파일된 클래스(상위 메서드가 okhttp3.Interceptor 등일 때)는 소스가 아니므로 훑지 않는다.
            val others = declarations
                .mapNotNull { it.containingFile }
                .filter { it != current?.sourcePsi && it !is PsiCompiledElement && (it is KtFile || it is PsiJavaFile) }
                .distinct()
                .mapNotNull { it.toUElementOfType<UFile>() }
            return listOfNotNull(current) + others
        }

        private fun scan(
            file: UFile,
            declarations: Set<PsiElement>,
            depth: Int,
            calledByName: KtNamedDeclaration?,
        ): Boolean {
            var found = false
            fun check(node: UElement, target: PsiElement?) {
                if (!found && target != null && target.navigationElement in declarations &&
                    isInRequestCallback(node, depth + 1)
                ) {
                    found = true
                }
            }
            file.accept(object : AbstractUastVisitor() {
                override fun visitCallExpression(node: UCallExpression): Boolean {
                    check(node, node.resolve())
                    if (calledByName != null) {
                        val call = node.sourcePsi as? KtCallExpression
                        if (call != null && call.calleeExpression?.text == calledByName.name &&
                            PsiTreeUtil.isAncestor(calledByName.parent, call, true) && !isShadowed(call, calledByName)
                        ) {
                            check(node, calledByName)
                        }
                    }
                    return found
                }

                override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                    // 커스텀 getter 는 프로퍼티를 읽을 때마다 돈다.
                    check(node, node.resolve())
                    return found
                }
            })
            return found
        }

        /**
         * 호출 위치와 [holder] 사이에 같은 이름으로 부를 수 있는 다른 선언(로컬 함수, 람다를 담은 변수, 함수 타입 파라미터)이
         * 있어 그쪽이 불리는지. 지역 변수는 자기 초기화식 안에서는 아직 보이지 않으므로(val token = token()) 제외한다.
         */
        private fun isShadowed(call: KtCallExpression, holder: KtNamedDeclaration): Boolean {
            val name = holder.name ?: return false
            var element: PsiElement? = call.parent
            while (element != null && element != holder.parent) {
                val shadows = when (element) {
                    is KtBlockExpression -> element.statements.any {
                        it is KtNamedDeclaration && it != holder && it.name == name && it.isCallable() &&
                            it.textOffset < call.textOffset &&
                            (it is KtNamedFunction || !PsiTreeUtil.isAncestor(it, call, false))
                    }
                    is KtFunction -> element.valueParameters.any { it.name == name && it.isCallable() }
                    else -> false
                }
                if (shadows) return true
                element = element.parent
            }
            return false
        }

        /** 이름 뒤에 ()를 붙여 부를 수 있는 선언인지. 타입을 알 수 없는 변수는 람다를 담았을 때만 그렇다고 본다. */
        private fun KtNamedDeclaration.isCallable(): Boolean = when (this) {
            is KtNamedFunction -> true
            is KtProperty -> initializer is KtLambdaExpression || typeReference?.text?.contains("->") == true
            is KtParameter -> typeReference?.text?.contains("->") == true
            else -> false
        }
    }

    companion object {
        private val REQUEST_CALLBACK_TYPES = setOf("okhttp3.Interceptor", "okhttp3.Authenticator")
        private val REQUEST_CALLBACK_METHODS = setOf("intercept", "authenticate")
        private val OKHTTP_BUILDER_CALLBACKS = listOf("addInterceptor", "addNetworkInterceptor", "authenticator")

        /** 콜백 → 보조 함수 → 보조 함수 … 를 따라가는 최대 깊이. */
        private const val MAX_HELPER_DEPTH = 4

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "BlockingCallInRequestPath",
            briefDescription = "요청 경로에서 runBlocking 사용",
            explanation = """
                OkHttp Interceptor/Authenticator 와 Ktor defaultRequest 는 요청을 시작한 스레드 \
                (대개 메인)에서 실행됩니다. 이 경로에서 runBlocking 으로 DataStore 등을 읽으면 \
                모든 API 호출이 디스크 I/O 만큼 메인 스레드를 정지시킵니다.

                토큰 적재는 suspend 를 지원하는 경로(Ktor Auth 의 loadTokens 등)로 옮기거나, \
                메모리에 캐시된 값을 사용하세요.
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                BlockingCallInRequestPathDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )
    }
}
