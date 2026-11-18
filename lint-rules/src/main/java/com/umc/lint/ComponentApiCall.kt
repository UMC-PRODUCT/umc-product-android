package com.umc.lint

import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod

/** 공용 컴포넌트를 구현하는 모듈(:presentation:component)의 패키지. 여기서는 원본 API를 감싸야 하므로 허용한다. */
internal const val COMPONENT_PACKAGE = "com.umc.component"

internal fun JavaContext.isInComponentPackage(): Boolean {
    val filePackage = uastFile?.packageName.orEmpty()
    return filePackage == COMPONENT_PACKAGE || filePackage.startsWith("$COMPONENT_PACKAGE.")
}

/**
 * 라이브러리에서 읽은 함수 이름을 소스 이름으로 되돌린다.
 * value class(Color, TextUnit 등)를 받는 함수는 JVM 이름이 "Text--4IGK_g"처럼 바뀌어 있다.
 */
internal fun sourceName(jvmName: String): String = jvmName.substringBefore('-')

/** [method]가 [targets]에 적힌 (함수 이름 → 패키지 목록) 중 하나인지 확인한다. */
internal fun JavaContext.isOneOf(method: PsiMethod, targets: Map<String, Set<String>>): Boolean {
    val packages = targets[sourceName(method.name)] ?: return false
    val methodPackage = evaluator.getPackage(method)?.qualifiedName ?: return false
    return methodPackage in packages
}
