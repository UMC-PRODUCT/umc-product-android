package com.umc.presentation.notice.write

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeWritePolicyTest {
    @Test
    fun `역할과 겸임 및 기수 정보별 카테고리 순서와 라벨을 유지한다`() {
        val expected = mapOf(
            NoticeWriterRole.SUPER_ADMIN to listOf(WriteCategoryType.ALL_GISU, WriteCategoryType.GISU,
                WriteCategoryType.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER),
            NoticeWriterRole.CENTRAL_CORE to listOf(WriteCategoryType.ALL_GISU, WriteCategoryType.GISU,
                WriteCategoryType.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER),
            NoticeWriterRole.CENTRAL_STAFF to listOf(WriteCategoryType.GISU,
                WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER),
            NoticeWriterRole.CHAPTER_PRESIDENT to listOf(WriteCategoryType.GISU),
            NoticeWriterRole.SCHOOL_CORE to listOf(WriteCategoryType.SCHOOL_CORE),
            NoticeWriterRole.SCHOOL_ETC_ADMIN to listOf(WriteCategoryType.SCHOOL_CORE),
            NoticeWriterRole.SCHOOL_PART_LEADER to listOf(WriteCategoryType.SCHOOL_PART_LEADER),
        )
        val labels = mapOf(
            WriteCategoryType.ALL_GISU to AppStrings.NOTICE_WRITE_ALL_GISU,
            WriteCategoryType.GISU to AppStrings.NOTICE_WRITE_SPECIFIC_GISU,
            WriteCategoryType.CENTRAL_STAFF to AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL,
            WriteCategoryType.SCHOOL_CORE to AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE,
            WriteCategoryType.SCHOOL_PART_LEADER to AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER,
        )
        NoticeWriterRole.entries.forEach { role ->
            listOf(false, true).forEach { dual ->
                listOf(null, 0L, 12L).forEach { generation ->
                    val types = expected.getValue(role).filter { generation != null || it != WriteCategoryType.GISU } +
                        if (role == NoticeWriterRole.CHAPTER_PRESIDENT && dual) listOf(WriteCategoryType.SCHOOL_CORE)
                        else emptyList()
                    assertEquals("$role/$dual/$generation", types.map { WriteCategory(it, labels.getValue(it)) },
                        createCategoriesForGeneration(role, generation, dual))
                }
            }
        }
    }

    @Test
    fun `기수 번호가 아닌 대상 ID로 카테고리 기수 정보를 찾는다`() {
        val gisus = listOf(GisuItem(120, 12))
        assertEquals(listOf(WriteCategoryType.GISU),
            createCategories(NoticeWriterRole.CHAPTER_PRESIDENT, gisus, 120).map { it.type })
        assertEquals(emptyList<WriteCategory>(), createCategories(NoticeWriterRole.CHAPTER_PRESIDENT, gisus, 12))
        assertEquals(emptyList<WriteCategory>(), createCategories(NoticeWriterRole.CHAPTER_PRESIDENT, gisus, null))
        // 이 함수는 목록 정제가 아니라 카테고리 구성만 담당하므로, 기존의 0기 처리도 유지한다.
        assertEquals(listOf(WriteCategoryType.GISU), createCategories(
            NoticeWriterRole.CHAPTER_PRESIDENT, listOf(GisuItem(120, 0)), 120,
        ).map { it.type })
    }

    @Test
    fun `모든 역할 카테고리 겸임 조합의 칩 순서와 기본 힌트를 유지한다`() {
        val allPart = listOf(BoardChipType.ALL, BoardChipType.PART)
        val schoolCore = listOf(BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART)
        val part = listOf(BoardChipType.PART)
        val expected = mapOf(
            NoticeWriterRole.SUPER_ADMIN to mapOf(
                WriteCategoryType.GISU to listOf(BoardChipType.ALL, BoardChipType.CHAPTER, BoardChipType.SCHOOL, BoardChipType.PART),
                WriteCategoryType.SCHOOL_CORE to listOf(BoardChipType.SCHOOL),
                WriteCategoryType.SCHOOL_PART_LEADER to listOf(BoardChipType.SCHOOL),
            ),
            NoticeWriterRole.CENTRAL_CORE to mapOf(WriteCategoryType.GISU to allPart),
            NoticeWriterRole.CENTRAL_STAFF to mapOf(WriteCategoryType.GISU to allPart),
            NoticeWriterRole.CHAPTER_PRESIDENT to mapOf(WriteCategoryType.GISU to allPart),
            NoticeWriterRole.SCHOOL_CORE to WriteCategoryType.entries.associateWith { schoolCore },
            NoticeWriterRole.SCHOOL_ETC_ADMIN to WriteCategoryType.entries.associateWith { part },
            NoticeWriterRole.SCHOOL_PART_LEADER to WriteCategoryType.entries.associateWith { part },
        )
        NoticeWriterRole.entries.forEach { role ->
            WriteCategoryType.entries.forEach { category ->
                listOf(false, true).forEach { dual ->
                    val chips = if (role == NoticeWriterRole.CHAPTER_PRESIDENT && dual &&
                        category == WriteCategoryType.SCHOOL_CORE) schoolCore
                    else expected.getValue(role)[category].orEmpty()
                    val hint = AppStrings.NOTICE_WRITE_GISU_CLASS_HINT.takeIf {
                        role == NoticeWriterRole.SUPER_ADMIN && category == WriteCategoryType.GISU
                    }
                    assertEquals("$role/$category/$dual", chips to hint, createBoardChips(role, category, dual))
                }
            }
        }
    }

    @Test
    fun `카테고리별 허용 기수와 선택 유지 계약을 보존한다`() {
        val gisus = listOf(GisuItem(120, 12), GisuItem(90, 9))
        val staffCategories = setOf(WriteCategoryType.CENTRAL_STAFF,
            WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER)
        NoticeWriterRole.entries.forEach { role ->
            (WriteCategoryType.entries + listOf(null)).forEach { category ->
                listOf(null, 120, 999).forEach { authority ->
                    val expected = if (role == NoticeWriterRole.SUPER_ADMIN ||
                        (role == NoticeWriterRole.CENTRAL_CORE && category !in staffCategories)) gisus
                    else if (authority == 120) listOf(gisus.first()) else emptyList()
                    assertEquals("$role/$category/$authority", expected,
                        allowedNoticeTargetGisus(role, gisus, authority, category))
                }
            }
        }
        val first = WriteCategory(WriteCategoryType.ALL_GISU, "전체")
        val second = WriteCategory(WriteCategoryType.GISU, "새 라벨")
        assertEquals(second, selectNoticeCategory(listOf(first, second), second.copy(label = "이전 라벨")))
        assertEquals(first, selectNoticeCategory(listOf(first, second), null))
        assertEquals(first, selectNoticeCategory(listOf(first), second))
        assertNull(selectNoticeCategory(emptyList(), second))
    }
}
