package com.umc.data.api

import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.HTTP
import retrofit2.http.OPTIONS
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.File
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.jar.JarFile
import kotlin.coroutines.Continuation

/**
 * Gson 이 (역)직렬화하는 프로젝트 클래스가 R8 keep 패키지 안에 있는지 확인한다.
 *
 * app/proguard-rules.pro 는 com.umc.data.request, com.umc.data.response, com.umc.domain.model 만 keep 한다.
 * 이 밖의 DTO 는 debug 에서는 멀쩡하지만 release 에서 필드명이 난독화돼 JSON 과 어긋난다.
 * Retrofit API 인터페이스의 @Body 와 응답 타입에서 출발해, 필드 타입을 따라가며 닿는 클래스를 모두 검사한다.
 */
class GsonDtoKeepPackageTest {

    @Test
    fun `API 인터페이스를 빠짐없이 찾는다`() {
        val found = apiInterfaces().map { it.simpleName }.toSet()
        val expected = setOf("AuthApi", "CommunityThreadApi", "CurriculumApi", "WorkbookApi", "NoticeApi", "StorageApi")
        assertTrue("찾지 못한 API: ${expected - found}", found.containsAll(expected))
    }

    @Test
    fun `Gson 이 다루는 프로젝트 클래스는 모두 keep 패키지 안에 있다`() {
        val violations = sortedMapOf<String, String>()
        val visited = mutableSetOf<String>()
        for (api in apiInterfaces()) {
            for (method in api.declaredMethods.filter(::isHttpMethod)) {
                val label = "${api.simpleName}.${method.name}"
                method.genericParameterTypes.forEachIndexed { index, type ->
                    if (method.parameterAnnotations[index].any { it is Body }) {
                        walk(type, emptyMap(), "$label @Body", visited, violations)
                    }
                }
                walk(responseType(method), emptyMap(), "$label 응답", visited, violations)
            }
        }
        assertTrue(
            "R8 keep 패키지(${KEEP_PACKAGES.joinToString()}) 밖에 있는 DTO:\n" +
                violations.entries.joinToString("\n") { (name, path) -> "  $name ← $path" } +
                "\nDTO 를 keep 패키지로 옮기세요.",
            violations.isEmpty(),
        )
    }

    private fun apiInterfaces(): List<Class<*>> {
        val loader = AuthApi::class.java.classLoader
        val location = File(AuthApi::class.java.protectionDomain.codeSource.location.toURI())
        val classNames = if (location.isDirectory) {
            File(location, API_PATH).listFiles().orEmpty().map { "$API_PATH/${it.name}" }
        } else {
            JarFile(location).use { jar -> jar.entries().toList().map { it.name }.filter { it.startsWith("$API_PATH/") } }
        }
        return classNames
            .filter { it.endsWith(".class") && '$' !in it.substringAfterLast('/') }
            .map { Class.forName(it.removeSuffix(".class").replace('/', '.'), false, loader) }
            .filter { it.isInterface && it.declaredMethods.any(::isHttpMethod) }
    }

    private fun isHttpMethod(method: Method): Boolean =
        !Modifier.isStatic(method.modifiers) && !method.isSynthetic &&
            method.annotations.any { it.annotationClass.java in HTTP_ANNOTATIONS }

    /** suspend 함수는 반환 타입이 Object 라서 마지막 파라미터 Continuation<? super T> 에서 T 를 꺼낸다. */
    private fun responseType(method: Method): Type {
        val last = method.genericParameterTypes.lastOrNull()
        if (last is ParameterizedType && last.rawType == Continuation::class.java) {
            val argument = last.actualTypeArguments[0]
            return if (argument is WildcardType) argument.lowerBounds.firstOrNull() ?: argument.upperBounds[0] else argument
        }
        return method.genericReturnType
    }

    private fun walk(
        type: Type,
        bindings: Map<String, Type>,
        path: String,
        visited: MutableSet<String>,
        violations: MutableMap<String, String>,
    ) {
        when (type) {
            is Class<*> -> visitClass(type, emptyList(), path, visited, violations)
            is ParameterizedType -> {
                val arguments = type.actualTypeArguments.map { resolve(it, bindings) }
                visitClass(type.rawType as Class<*>, arguments, path, visited, violations)
            }
            is WildcardType -> (type.lowerBounds + type.upperBounds).forEach { walk(it, bindings, path, visited, violations) }
            is GenericArrayType -> walk(type.genericComponentType, bindings, path, visited, violations)
            is TypeVariable<*> -> bindings[type.name]?.let { walk(it, bindings, path, visited, violations) }
        }
    }

    private fun resolve(type: Type, bindings: Map<String, Type>): Type =
        if (type is TypeVariable<*>) bindings[type.name] ?: type else type

    private fun visitClass(
        cls: Class<*>,
        arguments: List<Type>,
        path: String,
        visited: MutableSet<String>,
        violations: MutableMap<String, String>,
    ) {
        if (cls.isArray) return walk(cls.componentType, emptyMap(), path, visited, violations)
        // 컬렉션·래퍼·외부 라이브러리 타입은 타입 인자만 따라간다. keep 대상인지는 프로젝트 클래스만 본다.
        if (!cls.name.startsWith("com.umc.")) {
            arguments.forEach { walk(it, emptyMap(), path, visited, violations) }
            return
        }
        if (!visited.add(cls.name + arguments)) return
        if (KEEP_PACKAGES.none { cls.name.startsWith(it) }) violations.putIfAbsent(cls.name, path)
        if (cls.isEnum) return

        val bindings = cls.typeParameters.map { it.name }.zip(arguments).toMap()
        generateSequence(cls) { it.superclass }.takeWhile { it != Any::class.java }.forEach { owner ->
            owner.declaredFields
                .filter { !Modifier.isStatic(it.modifiers) && !Modifier.isTransient(it.modifiers) && !it.isSynthetic }
                .forEach { field ->
                    walk(field.genericType, bindings, "$path → ${owner.simpleName}.${field.name}", visited, violations)
                }
        }
    }

    private companion object {
        const val API_PATH = "com/umc/data/api"
        val KEEP_PACKAGES = listOf("com.umc.data.request.", "com.umc.data.response.", "com.umc.domain.model.")
        val HTTP_ANNOTATIONS = setOf(
            GET::class.java, POST::class.java, PUT::class.java, PATCH::class.java,
            DELETE::class.java, HEAD::class.java, OPTIONS::class.java, HTTP::class.java,
        )
    }
}
