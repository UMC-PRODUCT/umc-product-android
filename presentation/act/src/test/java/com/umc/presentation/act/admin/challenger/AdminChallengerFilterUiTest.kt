package com.umc.presentation.act.admin.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.act.challenger.ChallengerManagementFilters
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.*
import org.junit.Test

class AdminChallengerFilterUiTest {
    private val gisu = GisuItem(2, 12)
    private val organization = ChapterWithSchool(
        listOf(Chapter(4, "서울"), Chapter(5, "경기")),
        listOf(SchoolInfo(10, "가대학교"), SchoolInfo(20, "나대학교"), SchoolInfo(30, "다대학교")),
        mapOf(4 to listOf(10, 30), 5 to listOf(20)),
    )
    private val schoolUser = UserInfo(id = 1, schoolId = 10, roles = listOf(
        UserRole(1, 1, "SCHOOL_PRESIDENT", null, null, "SCHOOL", 10, null, 2, 12),
    ))

    private fun state(user: UserInfo = UserInfo(isSuperAdmin = true)) = AdminChallengerUiState(
        manageAccess = AdminChallengerAccess.ALLOWED,
        filterLoadState = AdminChallengerLoadState.SUCCESS,
        searchableGisus = persistentListOf(gisu),
        selectedGisu = gisu,
        searchFilters = ChallengerManagementFilters.create(user, gisu, organization),
    )

    @Test fun `단일 기수는 고정하고 여러 기수는 드롭다운으로 표시한다`() {
        assertTrue(state().filterUi().gisu.isFixed)
        val multiple = state().copy(searchableGisus = persistentListOf(gisu, GisuItem(90, 9)))
        assertFalse(multiple.filterUi().gisu.isFixed)
        assertEquals(2L, multiple.filterUi().gisu.selectedOption?.id)
        assertEquals("12기", multiple.filterUi().gisu.selectedOption?.label)
    }

    @Test fun `회장단의 지부와 학교는 고정값만 UI에 전달한다`() {
        val ui = state(schoolUser).filterUi()
        assertTrue(ui.chapter.isFixed)
        assertTrue(ui.school.isFixed)
        assertEquals(listOf(4L), ui.chapter.options.map { it.id })
        assertEquals(listOf(10L), ui.school.options.map { it.id })
        assertEquals("서울", ui.chapter.selectedOption?.label)
        assertEquals("가대학교", ui.school.selectedOption?.label)
    }

    @Test fun `조직 정보가 준비되지 않거나 권한이 없으면 검색을 열지 않는다`() {
        val ready = state()
        assertTrue(ready.filtersReady)
        assertFalse(ready.copy(searchFilters = null).filtersReady)
        assertFalse(ready.copy(filterLoadState = AdminChallengerLoadState.LOADING).filtersReady)
        assertFalse(ready.copy(filterLoadState = AdminChallengerLoadState.ERROR).filtersReady)
        assertFalse(ready.copy(manageAccess = AdminChallengerAccess.DENIED).filtersReady)
        val loading = ready.copy(searchFilters = null).filterUi()
        assertTrue(loading.chapter.isFixed)
        assertTrue(loading.school.isFixed)
    }
}
