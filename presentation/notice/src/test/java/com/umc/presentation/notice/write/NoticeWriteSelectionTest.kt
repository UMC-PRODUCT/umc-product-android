package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteSelectionTest {
    private val gisus = persistentListOf(
        GisuItem(gisuId = 12, generation = 12),
        GisuItem(gisuId = 9, generation = 9),
    )
    private val category = WriteCategory(WriteCategoryType.SCHOOL_CORE, "학교 회장단")
    private val school = SchoolInfo(7, "학교")
    private val selectedState = NoticeWriteUiState(
        writerRole = NoticeWriterRole.SUPER_ADMIN,
        targetGisuId = 9,
        selectedCategory = category,
        availableGisus = gisus,
        selectedSchool = school,
        schoolList = persistentListOf(school),
    )

    @Test
    fun `늦은 활성 기수 응답은 직접 고른 기수와 카테고리를 보존한다`() {
        val selection = resolve(selectedState)

        assertEquals(9, selection.targetGisuId)
        assertEquals(category, selection.previousCategory)
        assertFalse(selection.roleChanged)
        assertFalse(selection.resetUserSelection)
        // 실제 요청 생성까지 확인해 전체 기수 공지로 확대되지 않도록 한다.
        val target = buildNoticeTargetRequest(selectedState.copy(
            authorityGisuId = 12,
            targetGisuId = selection.targetGisuId,
            selectedCategory = selectNoticeCategory(listOf(
                WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"), category,
            ), selection.previousCategory),
        ))
        assertEquals(9, target?.targetGisuId)
        assertEquals(7, target?.targetSchoolId)
        assertEquals(emptyList<String>(), target?.targetParts)
        assertEquals("SCHOOL_CORE", target?.targetNoticeTab)
    }

    @Test
    fun `활성 기수의 학교와 파트로 지정한 일반 공지 대상도 보존한다`() {
        val state = selectedState.copy(
            selectedCategory = WriteCategory(WriteCategoryType.GISU, "특정 기수"),
            targetGisuId = 12,
            selectedPart = UserPart.PLAN,
        )
        val selection = resolve(state)
        val target = buildNoticeTargetRequest(state.copy(
            authorityGisuId = 12,
            targetGisuId = selection.targetGisuId,
            selectedCategory = selection.previousCategory,
        ))

        assertEquals(12, target?.targetGisuId)
        assertEquals(7, target?.targetSchoolId)
        assertEquals(listOf("PLAN"), target?.targetParts)
        assertEquals("CHALLENGER", target?.targetNoticeTab)
    }

    @Test
    fun `직접 선택하지 않은 기본값은 늦게 도착한 활성 기수를 따른다`() {
        val selection = resolve(selectedState, selectedGisu = false, selectedCategory = false)

        assertEquals(12, selection.targetGisuId)
        assertNull(selection.previousCategory)
        assertFalse(selection.resetUserSelection)
    }

    @Test
    fun `자동 기본값과 같은 기수를 직접 선택해도 사용자 선택으로 보존한다`() {
        val selection = resolve(selectedState.copy(selectedCategory = null))

        assertEquals(9, selection.targetGisuId)
    }

    @Test
    fun `카테고리만 직접 고르면 카테고리는 보존하고 기본 기수는 갱신한다`() {
        val selection = resolve(selectedState, selectedGisu = false)

        assertEquals(12, selection.targetGisuId)
        assertEquals(category, selection.previousCategory)
    }

    @Test
    fun `이미 확정된 권한 기수가 바뀌면 기존 선택을 초기화한다`() {
        val selection = resolve(selectedState.copy(authorityGisuId = 9))

        assertEquals(12, selection.targetGisuId)
        assertNull(selection.previousCategory)
        assertFalse(selection.roleChanged)
        assertTrue(selection.resetUserSelection)
    }

    @Test
    fun `역할이 바뀌면 직접 고른 선택도 새 권한 기준으로 초기화한다`() {
        val selection = resolveNoticeWriteSelection(
            state = selectedState,
            writerRole = NoticeWriterRole.CENTRAL_STAFF,
            authorityGisuId = 12,
            knownGisus = gisus,
            hasSelectedTargetGisu = true,
            hasSelectedCategory = true,
        )

        assertEquals(12, selection.targetGisuId)
        assertNull(selection.previousCategory)
        assertTrue(selection.roleChanged)
        assertTrue(selection.resetUserSelection)
    }

    @Test
    fun `선택한 기수가 허용 목록에서 사라지면 활성 기수로 되돌린다`() {
        val selection = resolveNoticeWriteSelection(
            state = selectedState,
            writerRole = NoticeWriterRole.SUPER_ADMIN,
            authorityGisuId = 12,
            knownGisus = gisus.take(1),
            hasSelectedTargetGisu = true,
            hasSelectedCategory = true,
        )

        assertEquals(12, selection.targetGisuId)
    }

    private fun resolve(
        state: NoticeWriteUiState,
        selectedGisu: Boolean = true,
        selectedCategory: Boolean = true,
    ) = resolveNoticeWriteSelection(
        state = state,
        writerRole = NoticeWriterRole.SUPER_ADMIN,
        authorityGisuId = 12,
        knownGisus = gisus,
        hasSelectedTargetGisu = selectedGisu,
        hasSelectedCategory = selectedCategory,
    )
}
