package com.umc.data.api

import com.umc.data.fixture.OutsideKeepDto
import com.umc.data.response.fixture.InheritedHolder
import com.umc.data.response.fixture.NestedHolder
import org.junit.Assert.assertEquals
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
import java.nio.file.Files
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
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

    @Test
    fun `중첩된 타입 인자와 상위 클래스의 타입 인자도 따라간다`() {
        // NestedEnvelope<T> 의 List<List<T>> 와, PayloadBase<OutsideKeepDto> 를 상속한 필드 payload: T 를 거쳐야 닿는다.
        val holders = listOf(NestedHolder::class.java, InheritedHolder::class.java)
        val found = holders.associate { holder ->
            val violations = sortedMapOf<String, String>()
            walk(holder, emptyMap(), holder.simpleName, mutableSetOf(), violations)
            holder.simpleName to violations.keys.toSet()
        }
        assertEquals(holders.associate { it.simpleName to setOf(OutsideKeepDto::class.java.name) }, found)
    }

    @Test
    fun `디렉터리와 JAR 모두 API 하위 패키지까지 같은 클래스 파일을 찾는다`() {
        val root = Files.createTempDirectory("api-classes").toFile()
        val jar = File(root.parentFile, "${root.name}.jar")
        val entries = listOf(
            "$API_PATH/AuthApi.class",
            "$API_PATH/extra/ExtraApi.class",
            "$API_PATH/AuthApi\$Companion.class",
            "com/umc/data/Other.class",
        )
        try {
            entries.forEach { File(root, it).apply { parentFile.mkdirs() }.writeBytes(ByteArray(0)) }
            JarOutputStream(jar.outputStream()).use { out -> entries.forEach { out.putNextEntry(JarEntry(it)); out.closeEntry() } }
            val expected = listOf("$API_PATH/AuthApi.class", "$API_PATH/extra/ExtraApi.class")
            assertEquals(expected, apiClassFiles(root).sorted())
            assertEquals(expected, apiClassFiles(jar).sorted())
        } finally {
            root.deleteRecursively()
            jar.delete()
        }
    }

    private fun apiInterfaces(): List<Class<*>> {
        val loader = AuthApi::class.java.classLoader
        val location = File(AuthApi::class.java.protectionDomain.codeSource.location.toURI())
        return apiClassFiles(location)
            .map { Class.forName(it.removeSuffix(".class").replace('/', '.'), false, loader) }
            .filter { it.isInterface && it.declaredMethods.any(::isHttpMethod) }
    }

    /** 컴파일 결과(디렉터리 또는 JAR)에서 API 패키지와 그 하위 패키지의 최상위 클래스 파일 경로. */
    private fun apiClassFiles(location: File): List<String> {
        val entries = if (location.isDirectory) {
            // JAR 쪽과 범위를 맞춰 하위 패키지까지 훑는다. 경로는 출력 루트 기준의 / 구분 경로로 만든다.
            File(location, API_PATH).walkTopDown().filter { it.isFile }.map { it.relativeTo(location).invariantSeparatorsPath }.toList()
        } else {
            JarFile(location).use { jar -> jar.entries().toList().map { it.name }.filter { it.startsWith("$API_PATH/") } }
        }
        return entries.filter { it.endsWith(".class") && '$' !in it.substringAfterLast('/') }
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

    /**
     * 타입 인자는 그 인자가 쓰인 곳의 바인딩과 함께 넘긴다. Envelope<T> 의 List<List<T>> 처럼 인자 안쪽에 남은 T 도,
     * Child : Base<Dto> 의 상위 클래스 필드 payload: T 도 실제 타입까지 따라가기 위해서다.
     */
    private class Bound(val type: Type, val context: Map<String, Bound>)

    private fun walk(
        type: Type,
        bindings: Map<String, Bound>,
        path: String,
        visited: MutableSet<String>,
        violations: MutableMap<String, String>,
    ) {
        when (type) {
            is Class<*> -> visitClass(type, emptyList(), path, visited, violations)
            is ParameterizedType -> {
                val arguments = type.actualTypeArguments.map { Bound(it, bindings) }
                visitClass(type.rawType as Class<*>, arguments, path, visited, violations)
            }
            is WildcardType -> (type.lowerBounds + type.upperBounds).forEach { walk(it, bindings, path, visited, violations) }
            is GenericArrayType -> walk(type.genericComponentType, bindings, path, visited, violations)
            is TypeVariable<*> -> bindings[type.name]?.let { walk(it.type, it.context, path, visited, violations) }
        }
    }

    /** 바인딩을 끝까지 풀어 쓴 타입 이름. 같은 클래스를 같은 타입 인자로 두 번 보지 않으려고 쓴다. */
    private fun describe(type: Type, bindings: Map<String, Bound>): String = when (type) {
        is Class<*> -> type.name
        is ParameterizedType -> (type.rawType as Class<*>).name +
            type.actualTypeArguments.joinToString(",", "<", ">") { describe(it, bindings) }
        is WildcardType -> (type.lowerBounds + type.upperBounds).joinToString(",", "?(", ")") { describe(it, bindings) }
        is GenericArrayType -> describe(type.genericComponentType, bindings) + "[]"
        is TypeVariable<*> -> bindings[type.name]?.let { describe(it.type, it.context) } ?: type.name
        else -> type.toString()
    }

    private fun visitClass(
        cls: Class<*>,
        arguments: List<Bound>,
        path: String,
        visited: MutableSet<String>,
        violations: MutableMap<String, String>,
    ) {
        if (cls.isArray) return walk(cls.componentType, emptyMap(), path, visited, violations)
        // 컬렉션·래퍼·외부 라이브러리 타입은 타입 인자만 따라간다. keep 대상인지는 프로젝트 클래스만 본다.
        if (!cls.name.startsWith("com.umc.")) {
            arguments.forEach { walk(it.type, it.context, path, visited, violations) }
            return
        }
        if (!visited.add(cls.name + arguments.map { describe(it.type, it.context) })) return
        if (KEEP_PACKAGES.none { cls.name.startsWith(it) }) violations.putIfAbsent(cls.name, path)
        if (cls.isEnum) return

        val bindings = cls.typeParameters.map { it.name }.zip(arguments).toMap()
        cls.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) && !Modifier.isTransient(it.modifiers) && !it.isSynthetic }
            .forEach { field ->
                walk(field.genericType, bindings, "$path → ${cls.simpleName}.${field.name}", visited, violations)
            }
        // 상위 클래스는 실제 타입 인자를 담은 genericSuperclass 로 따라간다. 상위 클래스도 필드가 직렬화되므로 keep 대상이다.
        cls.genericSuperclass?.let { walk(it, bindings, path, visited, violations) }
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
