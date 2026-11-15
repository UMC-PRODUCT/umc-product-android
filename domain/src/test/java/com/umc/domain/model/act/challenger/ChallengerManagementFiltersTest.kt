package com.umc.domain.model.act.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import org.junit.Assert.*
import org.junit.Test

class ChallengerManagementFiltersTest {
    private val newer = GisuItem(2, 12, false)
    private val older = GisuItem(90, 9, true)
    private val organization = ChapterWithSchool(
        listOf(Chapter(4, "서울"), Chapter(5, "경기")),
        listOf(SchoolInfo(10, "가대학교"), SchoolInfo(20, "나대학교")),
        mapOf(4 to listOf(10), 5 to listOf(20)),
    )
    private fun role(type: String, gisuId: Long = 2, school: Long? = 10, organizationType: String = "SCHOOL") = UserRole(
        1, 1, type, 99, "프로필의 다른 지부", organizationType, school, null, gisuId, 12,
    )
    private fun user(vararg roles: UserRole) = UserInfo(id = 1, schoolId = 10, roles = roles.toList())

    @Test fun `중앙 네 역할은 해당 기수에서만 전 학교를 조회한다`() {
        listOf("CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT", "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER").forEach {
            val member = user(role(it, school = 999))
            assertEquals(ChallengerManagementAccess.ALL_SCHOOLS, member.challengerManagementAccess(2))
            assertEquals(ChallengerManagementAccess.DENIED, member.challengerManagementAccess(90))
        }
    }

    @Test fun `회장단은 프로필 학교와 역할 학교가 같아야 한다`() {
        listOf("SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT").forEach {
            assertEquals(ChallengerManagementAccess.OWN_SCHOOL, user(role(it)).challengerManagementAccess(2))
            assertEquals(ChallengerManagementAccess.DENIED, user(role(it, school = 20)).challengerManagementAccess(2))
            assertEquals(ChallengerManagementAccess.DENIED, user(role(it, organizationType = "CHAPTER")).challengerManagementAccess(2))
            assertEquals(ChallengerManagementAccess.DENIED, user(role(it, school = null)).challengerManagementAccess(2))
            assertEquals(ChallengerManagementAccess.DENIED, user(role(it)).copy(schoolId = 0).challengerManagementAccess(2))
        }
    }

    @Test fun `지부장 파트장 기타 운영진과 알 수 없는 역할은 검색할 수 없다`() {
        listOf("CHAPTER_PRESIDENT", "SCHOOL_PART_LEADER", "SCHOOL_ETC_ADMIN", "UNKNOWN").forEach {
            assertTrue(user(role(it)).searchableChallengerGisus(listOf(newer, older)).isEmpty())
        }
        assertTrue(UserInfo().searchableChallengerGisus(listOf(newer)).isEmpty())
    }

    @Test fun `최고 관리자는 본인 이력 없이도 모든 정상 기수를 선택한다`() {
        val member = UserInfo(id = 1, isSuperAdmin = true)
        assertEquals(listOf(newer, older), member.searchableChallengerGisus(listOf(older, newer, GisuItem())))
        assertEquals(ChallengerManagementAccess.DENIED, member.challengerManagementAccess(0))
    }

    @Test fun `기수별로 중앙과 회장단 범위가 전환되고 중앙 역할이 우선한다`() {
        val member = user(role("CENTRAL_EDUCATION_TEAM_MEMBER"), role("SCHOOL_PRESIDENT", gisuId = 90))
        assertFalse(ChallengerManagementFilters.create(member, newer, organization)!!.isOrganizationFixed)
        assertTrue(ChallengerManagementFilters.create(member, older, organization)!!.isOrganizationFixed)
        assertFalse(ChallengerManagementFilters.create(
            user(role("CENTRAL_PRESIDENT"), role("SCHOOL_PRESIDENT")), newer, organization,
        )!!.isOrganizationFixed)
    }

    @Test fun `기본 기수는 유효한 기존 선택 활성 기수 generation 순으로 결정한다`() {
        val gisus = listOf(older, newer)
        assertEquals(newer, gisus.initialChallengerGisu(2))
        assertEquals(older, gisus.initialChallengerGisu())
        assertEquals(older, gisus.initialChallengerGisu(999))
        assertEquals(newer, listOf(older.copy(isActive = false), newer).initialChallengerGisu())
        assertNull(emptyList<GisuItem>().initialChallengerGisu())
    }

    @Test fun `회장단의 지부는 역할에 남은 지부가 아닌 선택 기수의 학교 연결로 고정된다`() {
        val filters = ChallengerManagementFilters.create(user(role("SCHOOL_PRESIDENT")), newer, organization)!!
        assertEquals(4L, filters.chapterId)
        assertEquals(10L, filters.schoolId)
        assertSame(filters, filters.selectChapter(5))
        assertSame(filters, filters.selectSchool(20))
    }

    @Test fun `학교 연결이 없거나 두 지부로 모호하면 회장단 검색을 열지 않는다`() {
        val member = user(role("SCHOOL_PRESIDENT"))
        assertNull(ChallengerManagementFilters.create(member, newer, organization.copy(schoolIdsByChapter = emptyMap())))
        assertNull(ChallengerManagementFilters.create(member, newer, organization.copy(schoolIdsByChapter = mapOf(4 to listOf(10), 5 to listOf(10)))))
        assertNull(ChallengerManagementFilters.create(member, newer, organization.copy(schoolList = emptyList())))
    }

    @Test fun `지부 변경은 학교를 초기화하고 범위 밖의 학교 선택은 무시한다`() {
        val filters = ChallengerManagementFilters.create(user(role("CENTRAL_PRESIDENT")), newer, organization)!!
        val selected = filters.selectChapter(4).selectSchool(10)
        assertEquals(10L, selected.schoolId)
        assertEquals(listOf(10), selected.availableSchools.map { it.schoolId })
        assertSame(selected, selected.selectChapter(4))
        assertSame(selected, selected.selectSchool(20))
        assertSame(selected, selected.selectChapter(999))
        assertNull(selected.selectChapter(5).schoolId)
        assertNull(selected.selectSchool(null).schoolId)
    }
}
