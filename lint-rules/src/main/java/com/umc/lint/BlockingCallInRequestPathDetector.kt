package com.umc.lint

import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import java.io.File
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UCallableReferenceExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UPostfixExpression
import org.jetbrains.uast.UPrefixExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.skipParenthesizedExprDown
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.UUnaryExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastPostfixOperator
import org.jetbrains.uast.UastPrefixOperator
import org.jetbrains.uast.toUElement
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

    /** 기본값이 있는 생성자 프로퍼티의 람다처럼 UAST 가 같은 호출을 두 번 보여 줄 때가 있어, 파일마다 한 번만 보고한다. */
    private val reportedInFile = mutableSetOf<PsiElement>()

    override fun beforeCheckFile(context: Context) {
        reportedInFile.clear()
    }

    override fun getApplicableMethodNames(): List<String> = listOf("runBlocking")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (method.containingClass?.qualifiedName?.startsWith("kotlinx.coroutines") != true) return
        if (node.sourcePsi in reportedInFile) return
        if (!RequestPathSearch(context).isInRequestCallback(node, depth = 0)) return
        node.sourcePsi?.let(reportedInFile::add)

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
     * - 위 콜백이 쓰는 보조 코드(여러 단계도 따라간다): 함수(함수 참조·operator 호출 포함), 로컬 함수, 커스텀 getter·setter,
     *   람다를 담은 프로퍼티·변수, 기반 클래스의 콜백이 부르는 훅을 재정의한 메서드. 호출 위치는 현재 파일, 선언이 있는 파일,
     *   그리고 다른 파일에서도 부를 수 있는 선언이면 같은 모듈의 다른 소스 파일에서 찾는다. 다른 모듈의 호출은 따라가지 않는다.
     *   IDE 즉석 검사는 편집 중인 파일만 다시 분석하므로, 다른 파일만 고쳤을 때는 이 파일을 다시 열거나 Inspect Code 로 확인한다.
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
         * 호출 위치는 현재 파일, 선언이 들어 있는 소스 파일(기반 클래스가 다른 파일에 있는 경우), 그리고 다른 파일에서도
         * 부를 수 있는 선언이면 이름이 들어 있는 같은 모듈의 소스 파일에서 찾는다.
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
                val found = filesToScan(declarations).any { (file, targets) -> scan(file, targets, depth, calledByName) }
                if (!found) failedAtDepth[declarations] = depth
                return found
            } finally {
                visiting -= declarations
            }
        }

        /**
         * 훑을 파일과 그 파일에서 찾을 선언. 현재 파일과 선언이 있는 파일에서는 [declarations] 전부를 찾는다.
         * 같은 모듈의 다른 파일에서는 소스 선언만 찾는다. 라이브러리 상위 메서드(Function0.invoke, Provider.get 등)로
         * 해석된 호출은 그 타입을 쓰는 모든 곳이라, 수신자 타입을 모른 채 맞추면 관련 없는 구현을 오류로 만든다.
         */
        private fun filesToScan(declarations: Set<PsiElement>): List<Pair<UFile, Set<PsiElement>>> {
            val current = context.uastFile
            // 라이브러리의 컴파일된 클래스(상위 메서드가 okhttp3.Interceptor 등일 때)는 소스가 아니므로 훑지 않는다.
            val nearFiles = (listOfNotNull(current?.sourcePsi) + declarations.mapNotNull { it.containingFile }.filter(::isSourceFile))
                .distinct()
            val sourceDeclarations = declarations.filterTo(mutableSetOf()) { isSourceFile(it.containingFile) }
            val moduleFiles = moduleFilesUsing(declarations).filter { it !in nearFiles }
            return (nearFiles.map { it to declarations } + moduleFiles.map { it to sourceDeclarations })
                .mapNotNull { (file, targets) ->
                    val uFile = if (file == current?.sourcePsi) current else file.toUElementOfType<UFile>()
                    uFile?.let { it to targets }
                }
        }

        private fun isSourceFile(file: PsiFile?): Boolean =
            file != null && file !is PsiCompiledElement && (file is KtFile || file is PsiJavaFile)

        /**
         * 다른 파일에서도 부를 수 있는 선언이면, 같은 모듈의 다른 소스 파일 중 그 선언을 부를 수 있는 파일.
         * 본문에 Kotlin 이름이나 Java 가 보는 JVM 이름(getToken, @JvmName)이 없는 파일은 열지 않는다.
         * operator 함수는 호출부에 이름이 나오지 않으므로(provider(), store[key]) 이름으로 거르지 않는다.
         */
        private fun moduleFilesUsing(declarations: Set<PsiElement>): List<PsiFile> {
            val visible = declarations.filter { isVisibleFromOtherFiles(it, declarations) }
            if (visible.isEmpty()) return emptyList()
            if (visible.any(::isOperator)) return moduleFiles { true }
            val names = visible.flatMapTo(mutableSetOf()) { declaration ->
                val names = listOfNotNull((declaration as? KtNamedDeclaration)?.name, (declaration as? PsiMethod)?.name) +
                    runCatching { declaration.toLightMethods().map { it.name } }.getOrDefault(emptyList())
                // Kotlin 은 Java 의 getToken()·setToken(v) 를 loader.token 으로 부른다.
                names + names.mapNotNull(::syntheticPropertyName)
            }
            // 디스크가 아니라 PSI 의 본문을 읽어 IDE 에서 저장하지 않은 편집도 반영한다.
            return moduleFiles { file -> names.any(file.text::contains) }
        }

        /**
         * 같은 파일 밖에서도 부를 수 있는 선언인지. private·로컬이거나 private·로컬 클래스 안에 있으면 그 파일 안에서만 쓰인다.
         * 프로퍼티는 이 탐색이 접근자에서 시작했다면 그 접근자(private set 등)의 가시성도 본다.
         */
        private fun isVisibleFromOtherFiles(declaration: PsiElement, declarations: Set<PsiElement>): Boolean {
            // 같은 모듈의 Java 소스에 있는 상위 메서드(인터페이스 등). 컴파일된 라이브러리 메서드는 여기 오지 않는다.
            if (declaration is PsiMethod) {
                return isSourceFile(declaration.containingFile) && !declaration.hasModifierProperty(PsiModifier.PRIVATE) &&
                    generateSequence(declaration.containingClass) { it.containingClass }.none {
                        it.hasModifierProperty(PsiModifier.PRIVATE) || PsiUtil.isLocalOrAnonymousClass(it)
                    }
            }
            val visible = when (declaration) {
                is KtNamedFunction -> !declaration.isLocal
                is KtProperty -> !declaration.isLocal && declarations.none {
                    it is KtPropertyAccessor && it.property == declaration && it.hasModifier(KtTokens.PRIVATE_KEYWORD)
                }
                // class TokenHolder(val provide: () -> String = { … }) 처럼 람다를 담은 생성자 프로퍼티.
                is KtParameter -> declaration.hasValOrVar()
                // 생성자는 클래스 이름으로 불린다(KtConstructor.name 이 클래스 이름이다).
                is KtConstructor<*> -> true
                else -> false
            }
            if (!visible || (declaration as KtNamedDeclaration).hasModifier(KtTokens.PRIVATE_KEYWORD)) return false
            return generateSequence(PsiTreeUtil.getParentOfType(declaration, KtClassOrObject::class.java)) {
                PsiTreeUtil.getParentOfType(it, KtClassOrObject::class.java)
            }.none { it.isLocal || it.hasModifier(KtTokens.PRIVATE_KEYWORD) }
        }

        /**
         * 이름 없이 불리는 operator 함수인지. operator 를 재정의할 때는 키워드를 생략할 수 있으므로
         * (class P : () -> String { override fun invoke() }) 관례 이름을 재정의한 함수도 operator 로 본다.
         */
        private fun isOperator(declaration: PsiElement): Boolean {
            // Kotlin 은 시그니처가 맞는 Java 메서드도 operator 로 부른다(store["k"] 가 JStore.get).
            if (declaration is PsiMethod) return isSourceFile(declaration.containingFile) && isOperatorName(declaration.name)
            if (declaration !is KtNamedFunction) return false
            if (declaration.hasModifier(KtTokens.OPERATOR_KEYWORD)) return true
            val name = declaration.name ?: return false
            return declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD) && isOperatorName(name)
        }

        private fun isOperatorName(name: String): Boolean = name in OPERATOR_CONVENTION_NAMES || COMPONENT_N.matches(name)

        /** getToken·setToken 을 Kotlin 이 부르는 프로퍼티 이름(token). getURL 은 url 이다. 접두사가 없으면 null. */
        private fun syntheticPropertyName(name: String): String? {
            val prefix = listOf("get", "set").firstOrNull { name.length > it.length && name.startsWith(it) && name[it.length].isUpperCase() }
                ?: return null
            val rest = name.substring(prefix.length)
            val capitals = rest.takeWhile { it.isUpperCase() }.length
            return when {
                capitals == rest.length -> rest.lowercase()
                capitals > 1 -> rest.substring(0, capitals - 1).lowercase() + rest.substring(capitals - 1)
                else -> rest.replaceFirstChar { it.lowercaseChar() }
            }
        }

        /** 같은 모듈(현재 lint 프로젝트)의 다른 소스 파일 중 [accept] 를 만족하는 파일. */
        private fun moduleFiles(accept: (PsiFile) -> Boolean): List<PsiFile> {
            val current = context.psiFile ?: return emptyList()
            val currentPath = current.virtualFile?.path
            val fileSystem = current.virtualFile?.fileSystem ?: return emptyList()
            return moduleSourceFiles
                .filter { it.path != currentPath }
                .mapNotNull { file -> fileSystem.findFileByPath(file.path)?.let(current.manager::findFile) }
                .filter { file -> runCatching { accept(file) }.getOrDefault(false) }
        }

        private val moduleSourceFiles: List<File> by lazy {
            context.project.javaSourceFolders.flatMap { root ->
                root.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "java") }.toList()
            }
        }

        private fun scan(
            file: UFile,
            declarations: Set<PsiElement>,
            depth: Int,
            calledByName: KtNamedDeclaration?,
        ): Boolean {
            var found = false
            // 접근자에서 시작했다면 프로퍼티를 가리키는 곳 중 그 접근자가 실제로 도는 곳(getter 는 읽기, setter 는 쓰기)만 본다.
            val accessor = declarations.firstNotNullOfOrNull { it as? KtPropertyAccessor }
            val hasOperator = declarations.any(::isOperator)
            fun check(node: UElement, target: PsiElement?) {
                if (found || target == null) return
                val declaration = target.navigationElement
                if (declaration !in declarations) return
                if (accessor != null && (declaration is KtProperty || (declaration is KtParameter && declaration.hasValOrVar()))) {
                    val (reads, writes) = accessOf(node, target)
                    if (if (accessor.isGetter) !reads else !writes) return
                }
                if (isInRequestCallback(node, depth + 1)) found = true
            }
            file.accept(object : AbstractUastVisitor() {
                override fun visitCallExpression(node: UCallExpression): Boolean {
                    val resolved = node.resolve()
                    // Java 의 holder.getProvide() 는 람다를 꺼내기만 한다. 바로 .invoke() 를 붙일 때만 람다가 돈다.
                    if (calledByName == null || resolved?.navigationElement != calledByName || isInvokedRightAway(node)) {
                        check(node, resolved)
                    }
                    // holder.provide.invoke(), onToken?.invoke() 처럼 invoke 를 직접 붙여 부르면 수신자가 그 선언인지 본다.
                    if (calledByName != null && node.methodName == "invoke" && declarationOf(node.receiver) == calledByName) {
                        check(node, calledByName)
                    }
                    // 이름이 같은 다른 함수(오버로드 등)로 해석된 호출은 람다·로컬 함수 호출이 아니다.
                    val resolvedDeclaration = resolved?.navigationElement
                    val resolvedElsewhere = resolvedDeclaration is KtNamedFunction && resolvedDeclaration != calledByName
                    if (calledByName != null && !resolvedElsewhere) {
                        val call = node.sourcePsi as? KtCallExpression
                        if (call != null && call.calleeExpression?.text == calledByName.name) {
                            // 호출 이름을 UAST 로 해석할 수 있으면 실제 선언과 비교한다(with(other) { tokenProvider() } 같은
                            // 암시적 수신자도 구분된다). 해석되지 않을 때만 범위·수신자·가림 여부로 추정한다.
                            val target = calleeDeclaration(call)
                            val matches = if (target != null) {
                                target == calledByName
                            } else {
                                PsiTreeUtil.isAncestor(calledByName.parent, call, true) &&
                                    !hasOtherReceiver(call) && !isShadowed(call, calledByName)
                            }
                            if (matches) check(node, calledByName)
                        }
                    }
                    return found
                }

                override fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression): Boolean {
                    // 커스텀 getter 는 프로퍼티를 읽을 때마다 돈다. 람다를 담은 프로퍼티는 읽기만 해서는 람다가 돌지 않는다.
                    if (calledByName == null) check(node, node.resolve())
                    return found
                }

                override fun visitCallableReferenceExpression(node: UCallableReferenceExpression): Boolean {
                    // transform(request, ::attachToken) 처럼 함수 참조로 넘긴 함수는 받은 쪽이 부른다. 프로퍼티 참조는 읽기가 아니다.
                    // 로컬 함수는 UAST 가 감싼 변수로 해석되므로 원래 Kotlin 선언을 꺼낸다.
                    val resolved = node.resolve()
                    val target = resolved?.takeIf { it.navigationElement !is KtElement }?.toUElement()?.sourcePsi as? KtNamedFunction
                        ?: resolved
                    if (target?.navigationElement !is KtProperty && target?.navigationElement !is KtParameter &&
                        !isPassedToUnusedParameter(node)
                    ) {
                        check(node, target)
                    }
                    return found
                }

                // operator 함수는 이름 없이 불린다(store[key], a + b, count++). 찾는 선언에 있을 때만 해석한다.
                override fun visitArrayAccessExpression(node: UArrayAccessExpression): Boolean {
                    if (!hasOperator) return found
                    val get = node.resolve()
                    check(node, get)
                    // store[k] += v, store[k]++ 는 get 다음에 set 도 부르지만 UAST 는 get 으로만 해석한다.
                    val (reads, writes) = accessOf(node, get ?: return found)
                    if (reads && writes && get is PsiMethod) compoundSetTargets(node, get).forEach { check(node, it) }
                    return found
                }

                override fun visitBinaryExpression(node: UBinaryExpression): Boolean {
                    if (hasOperator) check(node, node.resolveOperator())
                    return found
                }

                override fun visitPrefixExpression(node: UPrefixExpression): Boolean {
                    if (hasOperator) check(node, node.resolveOperator())
                    return found
                }

                override fun visitPostfixExpression(node: UPostfixExpression): Boolean {
                    if (hasOperator) check(node, node.resolveOperator())
                    return found
                }
            })
            return found
        }

        /** 식이 가리키는 Kotlin 선언(프로퍼티·생성자 프로퍼티·지역 변수). 지역 변수는 UAST 가 감싼 변수로 해석되므로 원래 선언을 꺼낸다. */
        private fun declarationOf(expression: UExpression?): PsiElement? {
            val resolved = (expression?.skipParenthesizedExprDown() as? UResolvable)?.resolve() ?: return null
            val navigation = resolved.navigationElement
            if (navigation is KtElement) return navigation
            return resolved.toUElement()?.sourcePsi as? KtElement
        }

        /**
         * 함수 참조를 인수로 넘겼는데, 받는 소스 함수의 본문이 그 파라미터를 전혀 쓰지 않는지.
         * transform(request, ::attachToken) 에서 transform 이 request 만 돌려주면 attachToken 은 불리지 않는다.
         * 받는 함수가 라이브러리이거나 본문이 없으면(인터페이스 등) 알 수 없으므로 부른다고 본다.
         */
        private fun isPassedToUnusedParameter(reference: UCallableReferenceExpression): Boolean {
            var argument: UElement = reference
            while (argument.uastParent is UParenthesizedExpression) argument = argument.uastParent!!
            val call = argument.uastParent as? UCallExpression ?: return false
            val method = call.resolve() ?: return false
            val parameter = context.evaluator.computeArgumentMapping(call, method)
                .entries.firstOrNull { it.key.sourcePsi == argument.sourcePsi }?.value ?: return false
            val callee = method.navigationElement
            if (!isSourceFile(callee.containingFile)) return false
            val name = parameter.name
            return when (callee) {
                is KtNamedFunction -> {
                    val body = callee.bodyExpression ?: return false
                    PsiTreeUtil.findChildrenOfType(body, KtNameReferenceExpression::class.java).none { it.getReferencedName() == name }
                }
                is PsiMethod -> {
                    val body = callee.body ?: return false
                    PsiTreeUtil.findChildrenOfType(body, PsiReferenceExpression::class.java).none { it.referenceName == name }
                }
                else -> false
            }
        }

        /** 호출 이름이 가리키는 선언. 로컬 함수는 UAST 가 감싼 변수로 해석되므로 원래 Kotlin 선언을 꺼낸다. */
        private fun calleeDeclaration(call: KtCallExpression): PsiElement? {
            val resolved = (call.calleeExpression?.toUElement() as? UResolvable)?.resolve() ?: return null
            val navigation = resolved.navigationElement
            if (navigation is KtElement) return navigation
            return resolved.toUElement()?.sourcePsi as? KtElement
        }

        /**
         * 프로퍼티를 가리키는 [node] 가 값을 읽는지와 쓰는지. += 와 ++ 는 둘 다 한다.
         * Java 의 getToken()·setToken(v) 호출은 해석된 접근자 메서드로 구분한다.
         */
        private fun accessOf(node: UElement, target: PsiElement): Pair<Boolean, Boolean> {
            if (node is UCallExpression) {
                val setter = (target as? PsiMethod)?.let {
                    it.returnType?.canonicalText == "void" && it.parameterList.parametersCount > 0
                } == true
                return !setter to setter
            }
            val access = outermostAccess(node)
            return when (val user = access.uastParent) {
                is UBinaryExpression -> when {
                    user.operator !is UastBinaryOperator.AssignOperator || user.leftOperand.sourcePsi != access.sourcePsi -> true to false
                    user.operator == UastBinaryOperator.ASSIGN -> false to true
                    // holder.acc += 1 이 Acc.plusAssign 으로 풀리면 getter 로 꺼낸 객체를 바꿀 뿐 setter 는 부르지 않는다.
                    user.resolveOperator()?.name?.endsWith("Assign") == true -> true to false
                    else -> true to true
                }
                is UUnaryExpression -> {
                    val changes = user.operator in INCREMENT_OPERATORS
                    true to changes
                }
                else -> true to false
            }
        }

        /**
         * store[k] += v·store[k]++ 가 get 다음에 부르는 set. UAST 는 get 으로만 해석하므로 직접 고른다.
         * 수신자 타입의 멤버 set 중 인덱스 파라미터가 get 과 맞고 값 파라미터가 get 의 결과를 받을 수 있는 것만 본다.
         * 원소 타입에 plusAssign 같은 <op>Assign 이 있으면 Kotlin 은 그것을 부르고 set 은 부르지 않는다.
         * 확장 함수로 선언한 set 은 찾지 않는다.
         */
        private fun compoundSetTargets(node: UArrayAccessExpression, get: PsiMethod): List<PsiMethod> {
            val user = outermostAccess(node).uastParent as? UBinaryExpression
            val element = (get.returnType as? PsiClassType)?.resolve()
            val right = user?.rightOperand?.getExpressionType()
            var value = get.returnType
            val operatorName = user?.operator?.let(ASSIGN_OPERATOR_NAMES::get)
            if (operatorName != null && element != null) {
                // 오른쪽 값을 받을 수 있는 <op>Assign 이 있으면 Kotlin 은 그것을 부르고 set 은 부르지 않는다.
                // 이름만 같고 받을 수 없는 것(plusAssign(String) 에 Int)은 고르지 않는다.
                if (element.findMethodsByName(operatorName, true).any { it.acceptsSingle(right) }) return emptyList()
                // a[i] += b 는 set(i, a[i].plus(b)) 이므로 set 에 넘기는 값은 plus 의 결과다.
                val plain = operatorName.removeSuffix("Assign")
                element.findMethodsByName(plain, true).firstOrNull { it.acceptsSingle(right) }?.returnType?.let { value = it }
            }
            val receiver = (node.receiver.getExpressionType() as? PsiClassType)?.resolve() ?: return emptyList()
            val indexTypes = get.parameterList.parameters.map { it.type }
            val applicable = receiver.findMethodsByName("set", true).filter { set ->
                val parameters = set.parameterList.parameters.map { it.type }
                parameters.size == indexTypes.size + 1 &&
                    parameters.zip(indexTypes).all { (parameter, index) -> accepts(parameter, index) } &&
                    accepts(parameters.last(), value)
            }
            // 재정의된 상위 메서드는 빼고(재정의한 쪽이 불린다), 남은 오버로드 중 가장 구체적인 것만 고른다.
            val overridden = applicable.flatMap(::superMethodsOf).map { it.navigationElement }.toSet()
            val candidates = applicable.filter { it.navigationElement !in overridden }
            return candidates.filter { candidate -> candidates.none { other -> other !== candidate && isMoreSpecific(other, candidate) } }
        }

        private fun PsiMethod.acceptsSingle(argument: PsiType?): Boolean =
            parameterList.parametersCount == 1 && accepts(parameterList.parameters[0].type, argument)

        private fun superMethodsOf(method: PsiMethod): Set<PsiMethod> {
            val result = mutableSetOf<PsiMethod>()
            val pending = ArrayDeque(method.findSuperMethods().toList())
            while (pending.isNotEmpty()) {
                val next = pending.removeFirst()
                if (result.add(next)) pending.addAll(next.findSuperMethods())
            }
            return result
        }

        /** [a] 가 [b] 보다 구체적인지: a 의 파라미터를 모두 b 에 넘길 수 있고 그 반대는 아니다. */
        private fun isMoreSpecific(a: PsiMethod, b: PsiMethod): Boolean = fitsInto(a, b) && !fitsInto(b, a)

        private fun fitsInto(a: PsiMethod, b: PsiMethod): Boolean =
            a.parameterList.parameters.zip(b.parameterList.parameters).all { (from, to) ->
                isTypeVariable(to.type) || (!isTypeVariable(from.type) && to.type.isAssignableFrom(from.type))
            }

        /** [parameter] 자리에 [value] 를 넘길 수 있는지. 제네릭 타입 변수는 대입을 알 수 없으므로 받는다고 본다. */
        private fun accepts(parameter: PsiType, value: PsiType?): Boolean {
            if (value == null || isTypeVariable(parameter) || isTypeVariable(value)) return true
            return parameter.isAssignableFrom(value)
        }

        private fun isTypeVariable(type: PsiType): Boolean = (type as? PsiClassType)?.resolve() is PsiTypeParameter

        /** holder.getProvide().invoke() 처럼 꺼낸 람다를 바로 부르는지. */
        private fun isInvokedRightAway(call: UCallExpression): Boolean {
            val expression = outermostAccess(call)
            val outer = expression.uastParent as? UQualifiedReferenceExpression ?: return false
            return outer.receiver.sourcePsi == expression.sourcePsi && (outer.selector as? UCallExpression)?.methodName == "invoke"
        }

        /** a.b.token 의 token 처럼 수신자 체인의 끝(selector)이면 체인 전체, 괄호로 감쌌으면 괄호까지 포함한 식. */
        private fun outermostAccess(node: UElement): UElement {
            var expression = node
            val parent = node.uastParent
            if (parent is UQualifiedReferenceExpression && parent.selector.sourcePsi == node.sourcePsi) expression = parent
            while (true) expression = expression.uastParent as? UParenthesizedExpression ?: return expression
        }

        /** other.tokenProvider() 처럼 this 가 아닌 수신자로 부르면 다른 객체의 같은 이름 프로퍼티다. */
        private fun hasOtherReceiver(call: KtCallExpression): Boolean {
            val qualified = call.parent as? KtQualifiedExpression ?: return false
            if (qualified.selectorExpression != call) return false
            return qualified.receiverExpression !is KtThisExpression
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

        /** 이름 뒤에 ()를 붙여 부를 수 있는 선언인지. 함수이거나 함수 타입 변수·파라미터면 그렇다. */
        private fun KtNamedDeclaration.isCallable(): Boolean = when (this) {
            is KtNamedFunction -> true
            is KtProperty, is KtParameter -> (this as KtCallableDeclaration).hasFunctionType()
            else -> false
        }

        /** 람다 초기화식이나 명시 타입으로 알 수 없으면 UAST 가 추론한 타입(val p = provider() 등)을 본다. */
        private fun KtCallableDeclaration.hasFunctionType(): Boolean {
            if (this is KtProperty && initializer is KtLambdaExpression) return true
            if (typeReference?.text?.contains("->") == true) return true
            val type = toUElementOfType<UVariable>()?.type?.canonicalText ?: return false
            return FUNCTION_TYPE_PREFIXES.any { type.startsWith(it) }
        }
    }

    companion object {
        private val REQUEST_CALLBACK_TYPES = setOf("okhttp3.Interceptor", "okhttp3.Authenticator")
        private val REQUEST_CALLBACK_METHODS = setOf("intercept", "authenticate")
        private val OKHTTP_BUILDER_CALLBACKS = listOf("addInterceptor", "addNetworkInterceptor", "authenticator")
        /**
         * 이름 없이 불리는 operator 관례 이름. equals 는 == 마다 불려 모듈 전체를 훑게 되므로 넣지 않는다.
         * 위임(getValue·setValue)·구조 분해(componentN)·for(iterator) 는 아직 호출 위치를 해석하지 않는다.
         */
        private val OPERATOR_CONVENTION_NAMES = setOf(
            "invoke", "get", "set", "contains", "getValue", "setValue", "provideDelegate", "iterator", "next", "hasNext",
            "compareTo", "plus", "minus", "times", "div", "rem", "rangeTo", "rangeUntil", "unaryPlus", "unaryMinus", "not",
            "inc", "dec", "plusAssign", "minusAssign", "timesAssign", "divAssign", "remAssign",
        )
        private val COMPONENT_N = Regex("component\\d+")

        /** 복합 대입 연산자와 Kotlin 이 먼저 찾는 operator 이름(a[i] += b 는 a[i].plusAssign(b) 가 있으면 그것을 부른다). */
        private val ASSIGN_OPERATOR_NAMES = mapOf(
            UastBinaryOperator.PLUS_ASSIGN to "plusAssign",
            UastBinaryOperator.MINUS_ASSIGN to "minusAssign",
            UastBinaryOperator.MULTIPLY_ASSIGN to "timesAssign",
            UastBinaryOperator.DIVIDE_ASSIGN to "divAssign",
            UastBinaryOperator.REMAINDER_ASSIGN to "remAssign",
        )
        private val INCREMENT_OPERATORS = setOf(
            UastPrefixOperator.INC, UastPrefixOperator.DEC, UastPostfixOperator.INC, UastPostfixOperator.DEC,
        )
        private val FUNCTION_TYPE_PREFIXES = listOf(
            "kotlin.jvm.functions.Function", "kotlin.Function", "kotlin.reflect.KFunction", "kotlin.coroutines.SuspendFunction",
        )

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
