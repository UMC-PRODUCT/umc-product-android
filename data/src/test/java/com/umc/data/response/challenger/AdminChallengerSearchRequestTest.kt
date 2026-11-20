package com.umc.data.response.challenger

import com.umc.data.api.ChallengerApi
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSourceImpl
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class AdminChallengerSearchRequestTest {
    @Test fun `관리 검색은 기수 지부 학교와 페이지 조건을 v2 API에 보낸다`() = runBlocking {
        var methodName = ""
        var received: List<Any?> = emptyList()
        val api = Proxy.newProxyInstance(ChallengerApi::class.java.classLoader, arrayOf(ChallengerApi::class.java)) { _, method, args ->
            methodName = method.name
            val getAnnotation = requireNotNull(method.getAnnotation(GET::class.java)) {
                "관리 검색 API에 @GET 어노테이션이 필요합니다."
            }
            assertEquals("api/v2/challenger/admin/search", getAnnotation.value)
            received = args!!.dropLast(1)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerSearchV2Response())
        } as ChallengerApi
        val remote: ChallengerRemoteDataSource = ChallengerRemoteDataSourceImpl(api)
        val response = remote.getAdminChallengerList(cursor = 3, size = 100, gisuId = 2, chapterId = 4, schoolId = 10, part = "ANDROID", keyword = "이름")
        assertTrue(response is ApiState.Success)
        assertEquals("getAdminChallengers", methodName)
        assertEquals(listOf(3, 100, "ANDROID", "이름", 10L, 4L, 2L), received)
        val queryNames = ChallengerApi::class.java.declaredMethods.single { it.name == "getAdminChallengers" }
            .parameterAnnotations.take(7)
            .map { annotations -> annotations.filterIsInstance<Query>().single().value }
        assertEquals(listOf("page", "size", "part", "keyword", "schoolId", "chapterId", "gisuId"), queryNames)
    }

    @Test fun `일반 전체 학교 검색은 기존 경로와 검색 조건을 유지한다`() = runBlocking {
        var received: List<Any?> = emptyList()
        val api = Proxy.newProxyInstance(ChallengerApi::class.java.classLoader, arrayOf(ChallengerApi::class.java)) { _, method, args ->
            assertEquals("getChallengers", method.name)
            assertEquals("api/v2/challenger/search", method.getAnnotation(GET::class.java)!!.value)
            received = args!!.dropLast(1)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerSearchV2Response())
        } as ChallengerApi
        val remote = ChallengerRemoteDataSourceImpl(api)
        val response = remote.getChallengerList(cursor = 2, size = 50, schoolId = 10, gisuId = 3, part = "ANDROID", keyword = "이름")
        assertTrue(response is ApiState.Success)
        assertEquals(listOf(2, 50, "ANDROID", "이름", 10L, null, 3L), received)
    }

    @Test fun `관리자 상세와 일반 상세는 각각의 경로에 챌린저 ID를 전달한다`() = runBlocking {
        val calls = mutableListOf<Triple<String, String, Long>>()
        val api = Proxy.newProxyInstance(ChallengerApi::class.java.classLoader, arrayOf(ChallengerApi::class.java)) { _, method, args ->
            assertEquals("challengerId", method.parameterAnnotations.first().filterIsInstance<Path>().single().value)
            calls += Triple(method.name, method.getAnnotation(GET::class.java)!!.value, args!!.first() as Long)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerResponse(challengerId = args.first() as Long))
        } as ChallengerApi
        val remote = ChallengerRemoteDataSourceImpl(api)
        val adminResponse = remote.getAdminChallengerDetail(356)
        val normalResponse = remote.getChallengerDetail(357)
        assertTrue(adminResponse is ApiState.Success)
        assertTrue(normalResponse is ApiState.Success)
        assertEquals(
            listOf(
                Triple("getAdminChallengerDetail", "api/v2/challenger/admin/{challengerId}", 356L),
                Triple("getChallengerDetail", "api/v1/challenger/{challengerId}", 357L),
            ),
            calls,
        )
    }

    @Test fun `일반 회원 검색의 학교와 기수 전달은 유지된다`() = runBlocking {
        var received: List<Any?> = emptyList()
        val api = Proxy.newProxyInstance(ChallengerApi::class.java.classLoader, arrayOf(ChallengerApi::class.java)) { _, method, args ->
            assertEquals("searchMembers", method.name)
            assertEquals("api/v1/member/search", method.getAnnotation(GET::class.java)!!.value)
            received = args!!.dropLast(1)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerSearchV2Response())
        } as ChallengerApi
        val remote: ChallengerRemoteDataSource = ChallengerRemoteDataSourceImpl(api)
        remote.searchMembers(null, 100, schoolId = 10, gisuId = 2, part = "PLAN", keyword = "이름")
        assertEquals(listOf(0, 100, "PLAN", "이름", 10L, 2L), received)
    }
}
