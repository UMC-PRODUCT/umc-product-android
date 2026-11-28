package com.umc.presentation.notice.write

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticeWriteHintsTest {
    private val school = SchoolInfo(7, "중앙대학교")
    private val chapter = Chapter(5, "담당 지부")

    private fun state(role: NoticeWriterRole, category: WriteCategoryType) = NoticeWriteUiState(
        writerRole = role,
        selectedCategory = WriteCategory(category, category.name),
        targetGisuId = 120, authorityGisuId = 120,
        availableGisus = persistentListOf(GisuItem(120, 12), GisuItem(90, 9)),
        schoolList = persistentListOf(school), chapterList = persistentListOf(chapter),
        writerSchoolId = 7, writerSchoolName = school.schoolName,
        writerChapterId = 5, writerChapterName = chapter.name,
    )

    private fun assertHint(expected: String, state: NoticeWriteUiState) {
        assertNotNull("$expected 요청", buildNoticeTargetRequest(state))
        assertEquals(expected, formatNoticeRecipientHint(state))
    }

    @Test
    fun `Super Admin 특정 기수의 전체 지부 학교 파트 조합을 요청대로 요약한다`() {
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU)
        assertNull(formatNoticeRecipientHint(base))
        assertHint("12기 · UMC 전체 · 챌린저 대상", base.copy(isAllSelected = true))
        assertHint("12기 · 담당 지부 · 챌린저 대상", base.copy(selectedChapter = chapter))
        assertHint("12기 · 중앙대학교 · 챌린저 대상", base.copy(selectedSchool = school))
        UserPart.selectable.forEach { part ->
            assertHint("12기 · UMC 전체 · 챌린저 · ${part.label} 대상", base.copy(selectedPart = part))
            assertHint("12기 · 담당 지부 · 챌린저 · ${part.label} 대상",
                base.copy(selectedChapter = chapter, selectedPart = part))
            assertHint("12기 · 중앙대학교 · 챌린저 · ${part.label} 대상",
                base.copy(selectedSchool = school, selectedPart = part))
        }
    }

    @Test
    fun `중앙 역할별 카테고리를 작성자 역할 대신 요청의 수신 탭으로 요약한다`() {
        listOf(NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE).forEach { role ->
            assertHint("전체 기수 · 챌린저 대상", state(role, WriteCategoryType.ALL_GISU))
            assertHint("12기 · 중앙 운영진 대상", state(role, WriteCategoryType.CENTRAL_STAFF))
        }
        listOf(NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.CENTRAL_STAFF)
            .forEach { role ->
                assertHint("12기 · UMC 전체 · 학교 회장단 대상", state(role, WriteCategoryType.SCHOOL_CORE))
                assertHint("12기 · UMC 전체 · 학교 운영진 대상", state(role, WriteCategoryType.SCHOOL_PART_LEADER))
            }
        listOf(NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.CENTRAL_STAFF).forEach { role ->
            val base = state(role, WriteCategoryType.GISU)
            assertNull(formatNoticeRecipientHint(base))
            assertHint("12기 · UMC 전체 · 챌린저 대상", base.copy(isAllSelected = true))
            UserPart.selectable.forEach { part ->
                assertHint("12기 · UMC 전체 · 챌린저 · ${part.label} 대상", base.copy(selectedPart = part))
            }
        }
        assertNull(formatNoticeRecipientHint(state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.CENTRAL_STAFF)))
    }

    @Test
    fun `학교를 지정한 회장단과 운영진은 요청 학교를 요약한다`() {
        assertHint("12기 · 중앙대학교 · 학교 회장단 대상",
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_CORE).copy(selectedSchool = school))
        assertHint("12기 · 중앙대학교 · 학교 운영진 대상",
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_PART_LEADER).copy(selectedSchool = school))
    }

    @Test
    fun `지부장은 본인 지부의 챌린저 범위를 표시한다`() {
        val base = state(NoticeWriterRole.CHAPTER_PRESIDENT, WriteCategoryType.GISU)
        assertEquals("12기 · 담당 지부", formatNoticeRecipientHint(base))
        assertHint("12기 · 담당 지부 · 챌린저 대상", base.copy(isAllSelected = true))
        UserPart.selectable.forEach { part ->
            assertHint("12기 · 담당 지부 · 챌린저 · ${part.label} 대상", base.copy(selectedPart = part))
        }
    }

    @Test
    fun `교내 회장단과 기존 겸임 지부장의 챌린저 및 운영진 대상을 구분한다`() {
        listOf(NoticeWriterRole.SCHOOL_CORE, NoticeWriterRole.CHAPTER_PRESIDENT).forEach { role ->
            val base = state(role, WriteCategoryType.SCHOOL_CORE).copy(canWriteAsSchoolCore = true)
            assertEquals("12기 · 중앙대학교", formatNoticeRecipientHint(base))
            assertHint("12기 · 중앙대학교 · 챌린저 대상", base.copy(isAllSelected = true))
            assertHint("12기 · 중앙대학교 · 학교 운영진 대상", base.copy(isStaffSelected = true))
            UserPart.selectable.forEach { part ->
                assertHint("12기 · 중앙대학교 · 챌린저 · ${part.label} 대상", base.copy(selectedPart = part))
                assertNull(formatNoticeRecipientHint(base.copy(isStaffSelected = true, selectedPart = part)))
            }
        }
    }

    @Test
    fun `학교 파트장과 기타 운영진은 파트 선택 전 범위만 이후 챌린저 대상을 표시한다`() {
        listOf(NoticeWriterRole.SCHOOL_PART_LEADER, NoticeWriterRole.SCHOOL_ETC_ADMIN).forEach { role ->
            val category = if (role == NoticeWriterRole.SCHOOL_PART_LEADER) WriteCategoryType.SCHOOL_PART_LEADER
                else WriteCategoryType.SCHOOL_CORE
            val base = state(role, category)
            assertEquals("12기 · 중앙대학교", formatNoticeRecipientHint(base))
            assertEquals(AppStrings.NOTICE_WRITE_PART_REQUIRED, resolveNoticeBoardHint(base))
            UserPart.selectable.forEach { part ->
                assertHint("12기 · 중앙대학교 · 챌린저 · ${part.label} 대상", base.copy(selectedPart = part))
            }
        }
    }

    @Test
    fun `과거 기수 요약과 게시판 제한 안내는 서로 다른 영역에 표시한다`() {
        listOf(NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE).forEach { role ->
            val past = state(role, WriteCategoryType.GISU).copy(
                targetGisuId = 90, isAllSelected = true,
                boardChips = persistentListOf(BoardChipType.ALL, BoardChipType.PART),
                boardHint = AppStrings.NOTICE_WRITE_GISU_CLASS_HINT,
            )
            assertHint("9기 · UMC 전체 · 챌린저 대상", past)
            assertEquals(AppStrings.NOTICE_WRITE_PAST_PART_HINT, resolveNoticeBoardHint(past))
            assertNull(formatNoticeRecipientHint(past.copy(isAllSelected = false, selectedPart = UserPart.PLAN)))
        }
        val adminPast = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU).copy(targetGisuId = 90)
        assertHint("9기 · 담당 지부 · 챌린저 대상", adminPast.copy(selectedChapter = chapter))
        assertHint("9기 · 중앙대학교 · 챌린저 대상", adminPast.copy(selectedSchool = school))
        assertHint("9기 · UMC 전체 · 학교 회장단 대상",
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_CORE).copy(targetGisuId = 90))
        assertHint("9기 · 중앙대학교 · 학교 운영진 대상",
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_PART_LEADER)
                .copy(targetGisuId = 90, selectedSchool = school))
    }

    @Test
    fun `과거 안내는 PART 원본 구성에서만 우선하고 그 외에는 역할 안내와 기본 안내를 사용한다`() {
        val past = state(NoticeWriterRole.SCHOOL_PART_LEADER, WriteCategoryType.SCHOOL_PART_LEADER)
            .copy(targetGisuId = 90, boardChips = persistentListOf(BoardChipType.PART), boardHint = "기존 안내")
        assertEquals(AppStrings.NOTICE_WRITE_PAST_PART_HINT, resolveNoticeBoardHint(past))
        assertEquals(AppStrings.NOTICE_WRITE_PART_REQUIRED, resolveNoticeBoardHint(past.copy(targetGisuId = 120)))
        val admin = past.copy(writerRole = NoticeWriterRole.SUPER_ADMIN)
        assertEquals("기존 안내", resolveNoticeBoardHint(admin.copy(boardChips = persistentListOf())))
        assertEquals("기존 안내", resolveNoticeBoardHint(admin.copy(authorityGisuId = null)))
        assertEquals("기존 안내", resolveNoticeBoardHint(admin.copy(
            selectedCategory = WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"))))
    }

    @Test
    fun `유효하지 않은 요청과 확인되지 않은 이름을 더 넓은 대상으로 표시하지 않는다`() {
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU).copy(selectedSchool = school)
        assertNull(formatNoticeRecipientHint(base.copy(selectedChapter = chapter)))
        assertNull(formatNoticeRecipientHint(base.copy(schoolList = persistentListOf())))
        assertNull(formatNoticeRecipientHint(base.copy(targetGisuId = null)))
        assertNull(formatNoticeRecipientHint(base.copy(selectedPart = UserPart.WEB)))
        assertNull(formatNoticeRecipientHint(base.copy(writerSchoolId = 8,
            schoolList = persistentListOf(school.copy(schoolName = "")))))
        val fixed = state(NoticeWriterRole.SCHOOL_CORE, WriteCategoryType.SCHOOL_CORE)
        assertNull(formatNoticeRecipientHint(fixed.copy(writerSchoolId = null)))
        assertNull(formatNoticeRecipientHint(fixed.copy(targetGisuId = 90)))
        assertNull(formatNoticeRecipientHint(fixed.copy(writerSchoolName = null, schoolList = persistentListOf())))
        assertNull(formatNoticeRecipientHint(fixed.copy(isAllSelected = true, writerSchoolName = null,
            schoolList = persistentListOf())))
        assertNull(formatNoticeRecipientHint(fixed.copy(writerRole = null)))
    }

    @Test
    fun `화면 잔류 값보다 요청에서 실제 사용하는 조직과 파트를 요약한다`() {
        val all = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU).copy(
            isAllSelected = true, selectedSchool = school, selectedPart = UserPart.PLAN,
        )
        assertHint("12기 · UMC 전체 · 챌린저 대상", all)
        val fixed = state(NoticeWriterRole.SCHOOL_CORE, WriteCategoryType.SCHOOL_CORE).copy(
            isAllSelected = true, writerSchoolName = null,
        )
        assertHint("12기 · 중앙대학교 · 챌린저 대상", fixed)
    }

    @Test
    fun `운영진 선택은 파트를 해제하고 선택 중에는 파트 칩을 숨긴다`() {
        val base = state(NoticeWriterRole.SCHOOL_CORE, WriteCategoryType.SCHOOL_CORE).copy(
            boardChips = persistentListOf(BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART),
            selectedPart = UserPart.PLAN,
        )
        val staff = toggleNoticeStaffSelection(base)
        assertTrue(staff.isStaffSelected)
        assertNull(staff.selectedPart)
        assertFalse(staff.canSelectPart)
        assertFalse(BoardChipType.PART in staff.visibleBoardChips)
        assertHint("12기 · 중앙대학교 · 학교 운영진 대상", staff)
        val cleared = toggleNoticeStaffSelection(staff)
        assertFalse(cleared.isStaffSelected)
        assertTrue(cleared.canSelectPart)
        assertNull(cleared.selectedPart)
        assertEquals("12기 · 중앙대학교", formatNoticeRecipientHint(cleared))
        assertEquals(base.copy(isEditMode = true), toggleNoticeStaffSelection(base.copy(isEditMode = true)))
        assertEquals(base.copy(boardChips = persistentListOf()),
            toggleNoticeStaffSelection(base.copy(boardChips = persistentListOf())))
    }
}
