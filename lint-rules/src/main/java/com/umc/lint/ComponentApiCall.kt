package com.umc.lint

import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULabeledExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UParenthesizedExpression

/**
 * 공용 컴포넌트를 구현하는 모듈(:presentation:component)의 namespace. 이 모듈에서는 원본 API를 감싸야 하므로 허용한다.
 * 파일의 package 선언은 어느 모듈의 소스에서나 쓸 수 있어 모듈 소속을 증명하지 못한다. namespace는 각 모듈의
 * build.gradle.kts가 정하는 값이라 다른 모듈의 소스 파일이 흉내 낼 수 없다. 다만 AGP 8.x는 모듈 간 namespace 중복을
 * 막지 않으므로(android.uniquePackageNames 기본값 false), 새 모듈을 만들 때 component의 namespace를 복사하지 않는다.
 */
internal const val COMPONENT_NAMESPACE = "com.umc.component"

/** 검사 중인 파일이 :presentation:component 모듈에 속하는지. 모듈의 namespace를 알 수 없으면 false다. */
internal fun JavaContext.isInComponentModule(): Boolean = project.getPackage() == COMPONENT_NAMESPACE

/**
 * 라이브러리에서 읽은 함수 이름을 소스 이름으로 되돌린다.
 * - value class(Color, TextUnit 등)를 받는 함수는 JVM 이름이 "Text--4IGK_g"처럼 뒤에 해시가 붙는다.
 * - Java에서 숨기려는 함수는 @JvmName("-addInterceptor")처럼 앞에 '-'가 붙는다(OkHttp의 함수 타입 오버로드).
 */
internal fun sourceName(jvmName: String): String = jvmName.removePrefix("-").substringBefore('-')

/** [method]가 [targets]에 적힌 (함수 이름 → 패키지 목록) 중 하나인지 확인한다. */
internal fun JavaContext.isOneOf(method: PsiMethod, targets: Map<String, Set<String>>): Boolean {
    val packages = targets[sourceName(method.name)] ?: return false
    val methodPackage = evaluator.getPackage(method)?.qualifiedName ?: return false
    return methodPackage in packages
}

/** [call]이 [packageName]에 선언된 [name] 함수를 부르는지. import 별칭과 무관하게 해석된 대상을 보고, 해석되지 않으면 false다. */
internal fun JavaContext.calls(call: UCallExpression, name: String, packageName: String): Boolean {
    val method = call.resolve() ?: return false
    return sourceName(method.name) == name && evaluator.getPackage(method)?.qualifiedName == packageName
}

/** 람다를 인수로 받은 호출. label@{ … } 나 괄호로 감싼 람다도 건너뛰어 찾는다. */
internal fun ULambdaExpression.ownerCall(): UCallExpression? {
    var parent: UElement? = uastParent
    while (parent is ULabeledExpression || parent is UParenthesizedExpression) parent = parent.uastParent
    return parent as? UCallExpression
}
