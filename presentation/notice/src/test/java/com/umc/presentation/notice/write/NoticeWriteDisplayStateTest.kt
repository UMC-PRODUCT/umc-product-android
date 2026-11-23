package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteDisplayStateTest {
    private val gisus = persistentListOf(
        GisuItem(gisuId = 12, generation = 12),
        GisuItem(gisuId = 9, generation = 9),
    )
    private val state = NoticeWriteUiState(
        availableGisus = gisus,
        authorityGisuId = 12,
        selectedCategory = WriteCategory(WriteCategoryType.GISU, "특정 기수"),
    )

    @Test
    fun `Super Admin과 총괄단은 모든 기수를 선택할 수 있다`() {
        listOf(NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE).forEach { role ->
            val current = state.copy(writerRole = role)

            assertEquals(role.name, gisus, current.selectableTargetGisus)
            assertTrue(role.name, current.showTargetGisuSelector)
        }
    }

    @Test
    fun `일반 운영진은 권한 기수만 대상으로 사용하고 기수 선택기를 표시하지 않는다`() {
        NoticeWriterRole.entries.filter {
            it != NoticeWriterRole.SUPER_ADMIN && it != NoticeWriterRole.CENTRAL_CORE
        }.forEach { role ->
            val current = state.copy(writerRole = role)

            assertEquals(role.name, gisus.take(1), current.selectableTargetGisus)
            assertFalse(role.name, current.showTargetGisuSelector)
        }
    }

    @Test
    fun `전체 기수 카테고리에서는 기수 선택기를 표시하지 않는다`() {
        listOf(NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE).forEach { role ->
            val current = state.copy(
                writerRole = role,
                selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"),
            )

            assertFalse(role.name, current.showTargetGisuSelector)
        }
    }

    @Test
    fun `역할이 없으면 선택 목록과 기수 선택기를 제공하지 않는다`() {
        assertTrue(state.selectableTargetGisus.isEmpty())
        assertFalse(state.showTargetGisuSelector)
    }

    @Test
    fun `권한 기수가 없거나 목록에서 사라지면 일반 운영진의 선택 목록은 비어 있다`() {
        val current = state.copy(writerRole = NoticeWriterRole.SCHOOL_CORE)

        assertTrue(current.copy(authorityGisuId = null).selectableTargetGisus.isEmpty())
        assertTrue(current.copy(availableGisus = persistentListOf(gisus.last())).selectableTargetGisus.isEmpty())
    }

    @Test
    fun `상태를 복사해 기수와 카테고리를 바꾸면 표시 상태도 함께 바뀐다`() {
        val current = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE)
        val allGisu = current.copy(
            selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"),
            availableGisus = persistentListOf(gisus.last()),
        )

        assertTrue(current.showTargetGisuSelector)
        assertFalse(allGisu.showTargetGisuSelector)
        assertEquals(listOf(gisus.last()), allGisu.selectableTargetGisus)
    }

    @Test
    fun `카테고리 선택 전에도 Super Admin의 기수 선택기를 표시하는 기존 동작을 유지한다`() {
        assertTrue(state.copy(writerRole = NoticeWriterRole.SUPER_ADMIN, selectedCategory = null)
            .showTargetGisuSelector)
    }
    @Test
    fun `총괄단 운영진 카테고리는 활성 기수 하나의 선택기를 유지한다`() {
        val current = state.copy(writerRole = NoticeWriterRole.CENTRAL_CORE,
            selectedCategory = WriteCategory(WriteCategoryType.CENTRAL_STAFF, "중앙 운영진"))
        assertEquals(gisus.take(1), current.selectableTargetGisus)
        assertTrue(current.showTargetGisuSelector)
        assertEquals(12, noticeTargetGisuId(NoticeWriterRole.CENTRAL_CORE,
            WriteCategoryType.CENTRAL_STAFF, 9, 12))
    }

    @Test
    fun `과거 기수에서는 파트 칩을 숨긴다`() {
        val current = state.copy(writerRole = NoticeWriterRole.SUPER_ADMIN, targetGisuId = 9,
            boardChips = persistentListOf(com.umc.domain.model.enums.BoardChipType.PART))
        assertFalse(current.canSelectPart)
        assertTrue(current.visibleBoardChips.isEmpty())
        assertTrue(current.copy(targetGisuId = 12).canSelectPart)
    }
    @Test
    fun `과거 파트 안내는 원래 PART 선택이 가능한 카테고리 힌트에만 표시한다`() {
        val past = state.copy(writerRole = NoticeWriterRole.SUPER_ADMIN, targetGisuId = 9,
            boardChips = persistentListOf(com.umc.domain.model.enums.BoardChipType.PART))
        assertEquals(com.umc.component.theme.AppStrings.NOTICE_WRITE_PAST_PART_HINT, past.categoryHint)
        assertNull(past.copy(boardChips = persistentListOf()).categoryHint)
        assertNull(past.copy(targetGisuId = 12).categoryHint)
        assertNull(past.copy(authorityGisuId = null).categoryHint)
        assertNull(past.copy(selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수")).categoryHint)
    }
    @Test
    fun `단일 지부장 카테고리는 고정 기수와 지부를 표시한다`() {
        val category = WriteCategory(WriteCategoryType.GISU, "특정 기수")
        val current = state.copy(writerRole = NoticeWriterRole.CHAPTER_PRESIDENT, targetGisuId = 12,
            availableCategories = persistentListOf(category), selectedCategory = category,
            writerChapterId = 5, writerChapterName = "담당 지부")
        assertEquals(category, selectNoticeCategory(current.availableCategories, null))
        assertEquals("12기 · 담당 지부", current.categoryHint)
    }

    @Test
    fun `교내 파트장과 기타 운영진은 본교와 파트 필수를 표시한다`() {
        listOf(NoticeWriterRole.SCHOOL_PART_LEADER, NoticeWriterRole.SCHOOL_ETC_ADMIN).forEach { role ->
            val category = WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, "파트장")
            val current = state.copy(writerRole = role, targetGisuId = 12,
                availableCategories = persistentListOf(category), selectedCategory = category,
                writerSchoolId = 7, writerSchoolName = "본교")
            assertEquals(category, selectNoticeCategory(current.availableCategories, null))
            assertEquals("12기 · 본교 · 파트 선택 필수", current.categoryHint)
            assertTrue(current.copy(writerSchoolName = null).categoryHint!!.contains("본교 확인 필요"))
        }
    }
}
