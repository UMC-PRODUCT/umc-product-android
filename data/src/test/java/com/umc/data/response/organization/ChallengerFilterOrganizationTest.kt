package com.umc.data.response.organization

import com.google.gson.Gson
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
}
