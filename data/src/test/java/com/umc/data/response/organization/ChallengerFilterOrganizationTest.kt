package com.umc.data.response.organization

import com.google.gson.Gson
import com.umc.data.dataSource.OrganizationDataSource
import com.umc.data.repository.OrganizationRepositoryImpl
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ChallengerFilterOrganizationTest {
    @Test fun `기수별 지부 학교 연결과 기존 평탄 목록을 함께 보존한다`() {
        val json = """{"chapters":[{"chapterId":"4","chapterName":"서울","schools":[{"schoolId":"10","schoolName":"가대학교"}]},{"chapterId":"5","chapterName":"경기","schools":[{"schoolId":"20","schoolName":"나대학교"}]}]}"""
        val result = Gson().fromJson(json, ChapterWithSchoolsListResponse::class.java).toModel()
        assertEquals(listOf(4, 5), result.chapterList.map { it.id })
        assertEquals(listOf(10, 20), result.schoolList.map { it.schoolId })
        assertEquals(mapOf(4 to listOf(10), 5 to listOf(20)), result.schoolIdsByChapter)
    }

    @Test fun `기수 페이지 API의 content와 문자열 ID를 읽는다`() {
        val json = """{"content":[{"gisuId":"2","generation":"12","isActive":false}],"page":0,"hasNext":false}"""
        val result = Gson().fromJson(json, GisuPageResponse::class.java)
        assertEquals(2, result.content.single().gisuId)
        assertEquals(12, result.content.single().generation)
        assertFalse(result.content.single().isActive)
    }

    @Test fun `최고 관리자 기수 후보를 위해 마지막 페이지까지 조회한다`() = runBlocking {
        val pages = mutableListOf<Int>()
        val repository = OrganizationRepositoryImpl(source { page ->
            pages += page
            ApiState.Success(GisuPageResponse(listOf(GisuItemResponse(page + 1, page + 8, page == 0)), page, page == 0))
        })
        val result = repository.getSearchGisus() as ApiState.Success
        assertEquals(listOf(0, 1), pages)
        assertEquals(listOf(1, 2), result.data.gisuList.map { it.gisuId })
    }

    @Test fun `기수 후속 페이지 실패는 앞 페이지의 일부 성공으로 반환하지 않는다`() = runBlocking {
        val failure = ApiState.Fail(FailState(code = "503", message = "서버 오류"))
        val repository = OrganizationRepositoryImpl(source { page ->
            if (page == 0) ApiState.Success(GisuPageResponse(listOf(GisuItemResponse(1, 9)), 0, true)) else failure
        })
        assertEquals(failure, repository.getSearchGisus())
    }

    @Test fun `진행하지 않는 페이지와 hasNext만 있는 빈 응답은 실패한다`() = runBlocking {
        listOf(
            GisuPageResponse(listOf(GisuItemResponse(1, 9)), 7, true),
            GisuPageResponse(emptyList(), 0, true),
            Gson().fromJson("""{"content":[{"gisuId":1,"generation":9}],"page":0}""", GisuPageResponse::class.java),
            Gson().fromJson("""{"content":[{"gisuId":1,"generation":9}],"hasNext":false}""", GisuPageResponse::class.java),
        ).forEach { page ->
            assertTrue(OrganizationRepositoryImpl(source { ApiState.Success(page) }).getSearchGisus() is ApiState.Fail)
        }
    }

    private fun source(block: (Int) -> ApiState<GisuPageResponse>): OrganizationDataSource = Proxy.newProxyInstance(
        OrganizationDataSource::class.java.classLoader, arrayOf(OrganizationDataSource::class.java),
    ) { _, method, args ->
        check(method.name == "getGisuPage")
        block(args!![0] as Int)
    } as OrganizationDataSource
}
