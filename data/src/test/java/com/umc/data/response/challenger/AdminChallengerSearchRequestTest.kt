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
            assertEquals("api/v2/challenger/search", getAnnotation.value)
            received = args!!.dropLast(1)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerSearchV2Response())
        } as ChallengerApi
        val remote: ChallengerRemoteDataSource = ChallengerRemoteDataSourceImpl(api)
        val response = remote.getAdminChallengerList(cursor = 3, size = 100, gisuId = 2, chapterId = 4, schoolId = 10, part = "ANDROID", keyword = "이름")
        assertTrue(response is ApiState.Success)
        assertEquals("getChallengers", methodName)
        assertEquals(listOf(3, 100, "ANDROID", "이름", 10L, 4L, 2L), received)
        val queryNames = ChallengerApi::class.java.declaredMethods.single { it.name == "getChallengers" }
            .parameterAnnotations.take(7)
            .map { annotations -> annotations.filterIsInstance<Query>().single().value }
        assertEquals(listOf("page", "size", "part", "keyword", "schoolId", "chapterId", "gisuId"), queryNames)
    }

    @Test fun `일반 회원 검색의 학교와 기수 전달은 유지된다`() = runBlocking {
        var received: List<Any?> = emptyList()
        val api = Proxy.newProxyInstance(ChallengerApi::class.java.classLoader, arrayOf(ChallengerApi::class.java)) { _, method, args ->
            assertEquals("searchMembers", method.name)
            received = args!!.dropLast(1)
            ApiResponse(success = true, code = "OK", message = "", result = ChallengerSearchV2Response())
        } as ChallengerApi
        val remote: ChallengerRemoteDataSource = ChallengerRemoteDataSourceImpl(api)
        remote.searchMembers(null, 100, schoolId = 10, gisuId = 2, part = "PLAN", keyword = "이름")
        assertEquals(listOf(0, 100, "PLAN", "이름", 10L, 2L), received)
    }
}
