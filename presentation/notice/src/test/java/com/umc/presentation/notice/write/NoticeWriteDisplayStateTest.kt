package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
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
}
