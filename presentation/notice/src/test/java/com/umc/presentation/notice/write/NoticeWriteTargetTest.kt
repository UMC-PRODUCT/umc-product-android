package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.school.SchoolInfo
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeWriteTargetTest {
    @Test
    fun `카테고리 첫 조회와 기존 선택이 유효하지 않을 때 첫 항목을 선택한다`() {
        val categories = listOf(
            WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"),
            WriteCategory(WriteCategoryType.GISU, "특정 기수"),
        )

        assertEquals(categories.first(), selectNoticeCategory(categories, null))
        assertEquals(categories.first(), selectNoticeCategory(categories,
            WriteCategory(WriteCategoryType.SCHOOL_CORE, "학교 회장단")))
        assertNull(selectNoticeCategory(emptyList(), null))
    }

    @Test
    fun `사용자가 선택한 카테고리가 유효하면 첫 항목으로 덮어쓰지 않는다`() {
        val categories = listOf(
            WriteCategory(WriteCategoryType.ALL_GISU, "전체 기수"),
            WriteCategory(WriteCategoryType.GISU, "특정 기수"),
        )

        assertEquals(categories[1], selectNoticeCategory(categories,
            WriteCategory(WriteCategoryType.GISU, "이전 이름")))
    }

    @Test
    fun `Super Admin과 총괄단만 모든 대상 기수를 선택한다`() {
        val gisus = listOf(GisuItem(gisuId = 11, generation = 11), GisuItem(gisuId = 10, generation = 10))
        val limitedRoles = listOf(
            NoticeWriterRole.CENTRAL_STAFF,
            NoticeWriterRole.CHAPTER_PRESIDENT,
            NoticeWriterRole.SCHOOL_CORE,
            NoticeWriterRole.SCHOOL_ETC_ADMIN,
            NoticeWriterRole.SCHOOL_PART_LEADER,
        )

        limitedRoles.forEach { role ->
            assertEquals(role.toString(), listOf(gisus.first()), allowedNoticeTargetGisus(role, gisus, 11))
        }
        assertEquals(gisus, allowedNoticeTargetGisus(NoticeWriterRole.SUPER_ADMIN, gisus, 11))
        assertEquals(gisus, allowedNoticeTargetGisus(NoticeWriterRole.CENTRAL_CORE, gisus, 11))
    }

    @Test
    fun `총괄단은 전체 기수와 중앙 운영진 권한의 운영진 공지를 작성할 수 있다`() {
        val all = buildNoticeTargetRequest(state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.ALL_GISU))
        val central = buildNoticeTargetRequest(state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.CENTRAL_STAFF)
            .copy(isStaffSelected = true))

        assertNull(all?.targetGisuId)
        assertEquals("CHALLENGER", all?.targetNoticeTab)
        assertEquals(12, central?.targetGisuId)
        assertEquals("SCHOOL_CORE", central?.targetNoticeTab)
    }

    @Test
    fun `중앙 운영진은 중앙 운영진 권한으로 일반 공지와 운영진 공지를 작성한다`() {
        val base = state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.CENTRAL_STAFF)

        assertNull(buildNoticeTargetRequest(base))
        assertEquals("CHALLENGER", buildNoticeTargetRequest(base.copy(isAllSelected = true))?.targetNoticeTab)
        assertEquals(listOf("PLAN"), buildNoticeTargetRequest(base.copy(selectedPart = UserPart.PLAN))?.targetParts)
        assertEquals("SCHOOL_CORE", buildNoticeTargetRequest(base.copy(isStaffSelected = true))?.targetNoticeTab)
        val partStaff = buildNoticeTargetRequest(base.copy(isStaffSelected = true, selectedPart = UserPart.PLAN))
        assertEquals("SCHOOL_PART_LEADER", partStaff?.targetNoticeTab)
        assertEquals(listOf("PLAN"), partStaff?.targetParts)
        assertNull(partStaff?.targetSchoolId)

        val schoolCore = buildNoticeTargetRequest(state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE))
        assertEquals("SCHOOL_CORE", schoolCore?.targetNoticeTab)
        assertNull(schoolCore?.targetSchoolId)
    }

    @Test
    fun `총괄단도 중앙 운영진 권한의 칩 조합을 요청에 반영한다`() {
        val base = state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.CENTRAL_STAFF)

        assertNull(buildNoticeTargetRequest(base))
        val all = buildNoticeTargetRequest(base.copy(isAllSelected = true))
        assertEquals("CHALLENGER", all?.targetNoticeTab)
        assertEquals(emptyList<String>(), all?.targetParts)
        val generalPart = buildNoticeTargetRequest(base.copy(selectedPart = UserPart.DESIGN))
        assertEquals("CHALLENGER", generalPart?.targetNoticeTab)
        assertEquals(listOf("DESIGN"), generalPart?.targetParts)
        val staff = buildNoticeTargetRequest(base.copy(isStaffSelected = true))
        assertEquals("SCHOOL_CORE", staff?.targetNoticeTab)
        val staffPart = buildNoticeTargetRequest(base.copy(isStaffSelected = true, selectedPart = UserPart.DESIGN))
        assertEquals("SCHOOL_PART_LEADER", staffPart?.targetNoticeTab)
        assertEquals(listOf("DESIGN"), staffPart?.targetParts)
        assertNull(buildNoticeTargetRequest(base.copy(isAllSelected = true, isStaffSelected = true)))
    }

    @Test
    fun `Super Admin의 중앙 운영진 카테고리는 기존 중앙 운영진 공지로 유지한다`() {
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.CENTRAL_STAFF)

        assertEquals("CENTRAL_MEMBER", buildNoticeTargetRequest(base)?.targetNoticeTab)
        assertNull(buildNoticeTargetRequest(base.copy(isStaffSelected = true)))
    }

    @Test
    fun `Super Admin의 전체 기수 공지에는 학교를 지정하지 않는다`() {
        val school = SchoolInfo(7, "학교")
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.ALL_GISU)

        assertNull(buildNoticeTargetRequest(base)?.targetSchoolId)
        assertNull(buildNoticeTargetRequest(base.copy(selectedSchool = school, schoolList = persistentListOf(school))))
        assertNull(buildNoticeTargetRequest(base.copy(selectedSchool = SchoolInfo(-1, "잘못된 학교"))))
    }

    @Test
    fun `Super Admin은 학교 회장단과 학교 파트장 공지의 대상 학교를 지정한다`() {
        val school = SchoolInfo(7, "학교")
        val core = buildNoticeTargetRequest(state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_CORE)
            .copy(selectedSchool = school, schoolList = persistentListOf(school)))
        val leader = buildNoticeTargetRequest(state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_PART_LEADER)
            .copy(selectedSchool = school, selectedPart = UserPart.PLAN, schoolList = persistentListOf(school)))

        assertEquals(7, core?.targetSchoolId)
        assertEquals("SCHOOL_CORE", core?.targetNoticeTab)
        assertEquals(7, leader?.targetSchoolId)
        assertEquals(listOf("PLAN"), leader?.targetParts)
        assertEquals("SCHOOL_PART_LEADER", leader?.targetNoticeTab)
    }

    @Test
    fun `학교 칩이 없는 역할과 전체 기수 카테고리는 학교 ID를 보낼 수 없다`() {
        val school = SchoolInfo(7, "학교")
        val cases = listOf(
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.ALL_GISU)
                .copy(schoolList = persistentListOf(school)),
            state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.ALL_GISU)
                .copy(schoolList = persistentListOf(school)),
            state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.SCHOOL_CORE)
                .copy(schoolList = persistentListOf(school)),
            state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.SCHOOL_PART_LEADER)
                .copy(selectedPart = UserPart.PLAN, schoolList = persistentListOf(school)),
        )

        cases.forEach { current ->
            assertNull(current.writerRole.toString(), buildNoticeTargetRequest(current.copy(selectedSchool = school)))
        }
    }

    @Test
    fun `선택 기수 목록 밖의 지부와 학교는 공지 대상으로 보내지 않는다`() {
        val sudal = Chapter(7, "수달")
        val neon = Chapter(8, "Neon")
        val school = SchoolInfo(9, "11기 학교")
        val otherSchool = SchoolInfo(10, "다른 기수 학교")
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU).copy(
            chapterList = persistentListOf(sudal),
            schoolList = persistentListOf(school),
        )

        assertEquals(7, buildNoticeTargetRequest(base.copy(selectedChapter = sudal))?.targetChapterId)
        assertNull(buildNoticeTargetRequest(base.copy(selectedChapter = neon)))
        assertEquals(9, buildNoticeTargetRequest(base.copy(selectedSchool = school))?.targetSchoolId)
        assertNull(buildNoticeTargetRequest(base.copy(selectedSchool = otherSchool)))
    }

    @Test
    fun `지부장은 본인 지부 전체 또는 특정 파트에 일반 공지를 작성한다`() {
        val base = state(NoticeWriterRole.CHAPTER_PRESIDENT, WriteCategoryType.GISU)
            .copy(writerChapterId = 5)

        assertNull(buildNoticeTargetRequest(base))
        assertEquals(5, buildNoticeTargetRequest(base.copy(isAllSelected = true))?.targetChapterId)
        val partTarget = buildNoticeTargetRequest(base.copy(selectedPart = UserPart.PLAN))
        assertEquals(listOf("PLAN"), partTarget?.targetParts)
        assertEquals("CHALLENGER", partTarget?.targetNoticeTab)
    }

    @Test
    fun `지부장이 교내 회장 역할도 가지면 권한 기수의 본인 학교에만 작성한다`() {
        val base = state(NoticeWriterRole.CHAPTER_PRESIDENT, WriteCategoryType.SCHOOL_CORE).copy(
            canWriteAsSchoolCore = true,
            writerChapterId = 5,
            writerSchoolId = 7,
            availableGisus = persistentListOf(GisuItem(gisuId = 12, generation = 12), GisuItem(gisuId = 9, generation = 9)),
        )

        assertEquals(7, buildNoticeTargetRequest(base.copy(isAllSelected = true))?.targetSchoolId)
        assertEquals("SCHOOL_CORE", buildNoticeTargetRequest(base.copy(isStaffSelected = true))?.targetNoticeTab)
        assertNull(buildNoticeTargetRequest(base.copy(canWriteAsSchoolCore = false, isStaffSelected = true)))
        assertNull(buildNoticeTargetRequest(base.copy(
            targetGisuId = 9,
            isStaffSelected = true,
        )))
    }

    @Test
    fun `교내 회장단의 운영진 대상은 파트 지정에 따라 구분한다`() {
        val base = state(NoticeWriterRole.SCHOOL_CORE, WriteCategoryType.SCHOOL_CORE)
            .copy(writerSchoolId = 7, isStaffSelected = true)

        assertEquals("SCHOOL_CORE", buildNoticeTargetRequest(base)?.targetNoticeTab)
        val partTarget = buildNoticeTargetRequest(base.copy(selectedPart = UserPart.PLAN))
        assertEquals("SCHOOL_PART_LEADER", partTarget?.targetNoticeTab)
        assertEquals(7, partTarget?.targetSchoolId)
    }

    @Test
    fun `교내 기타 운영진은 학교와 파트를 모두 지정해야 한다`() {
        val base = state(NoticeWriterRole.SCHOOL_ETC_ADMIN, WriteCategoryType.SCHOOL_CORE)
            .copy(writerSchoolId = 7)

        assertNull(buildNoticeTargetRequest(base))
        assertNull(buildNoticeTargetRequest(base.copy(selectedPart = UserPart.PLAN, writerSchoolId = null)))
        val target = buildNoticeTargetRequest(base.copy(selectedPart = UserPart.PLAN))
        assertNotNull(target)
        assertEquals(7, target?.targetSchoolId)
        assertEquals(listOf("PLAN"), target?.targetParts)
    }

    @Test
    fun `파트장은 본인 학교라면 다른 파트도 지정할 수 있다`() {
        val base = state(NoticeWriterRole.SCHOOL_PART_LEADER, WriteCategoryType.SCHOOL_PART_LEADER)
            .copy(writerSchoolId = 7)

        assertNull(buildNoticeTargetRequest(base))
        assertEquals(listOf("DESIGN"), buildNoticeTargetRequest(base.copy(selectedPart = UserPart.DESIGN))?.targetParts)
    }

    @Test
    fun `시스템 관리자도 특정 기수 대상에는 기수 선택이 필요하다`() {
        val base = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_CORE)

        assertNull(buildNoticeTargetRequest(base.copy(targetGisuId = null)))
        assertEquals(12, buildNoticeTargetRequest(base)?.targetGisuId)
    }

    @Test
    fun `Super Admin과 총괄단은 다른 기수를 대상으로 작성한다`() {
        val otherGisu = GisuItem(gisuId = 9, generation = 9)
        val cases = listOf(
            state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.SCHOOL_CORE),
            state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.CENTRAL_STAFF)
                .copy(isStaffSelected = true),
        )

        cases.forEach { current ->
            val target = buildNoticeTargetRequest(current.copy(
                targetGisuId = otherGisu.gisuId,
                availableGisus = persistentListOf(GisuItem(gisuId = 12, generation = 12), otherGisu),
            ))
            assertEquals(current.writerRole.toString(), 9, target?.targetGisuId)
        }
    }

    @Test
    fun `총괄단 외의 작성자는 다른 기수에 작성할 수 없다`() {
        val otherGisu = GisuItem(gisuId = 9, generation = 9)
        val cases = listOf(
            state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE),
            state(NoticeWriterRole.CHAPTER_PRESIDENT, WriteCategoryType.GISU)
                .copy(writerChapterId = 5, isAllSelected = true),
            state(NoticeWriterRole.SCHOOL_CORE, WriteCategoryType.SCHOOL_CORE)
                .copy(writerSchoolId = 7, isAllSelected = true),
            state(NoticeWriterRole.SCHOOL_ETC_ADMIN, WriteCategoryType.SCHOOL_CORE)
                .copy(writerSchoolId = 7, selectedPart = UserPart.PLAN),
            state(NoticeWriterRole.SCHOOL_PART_LEADER, WriteCategoryType.SCHOOL_PART_LEADER)
                .copy(writerSchoolId = 7, selectedPart = UserPart.PLAN),
        )

        cases.forEach { current ->
            assertNull(current.writerRole.toString(), buildNoticeTargetRequest(current.copy(
                targetGisuId = otherGisu.gisuId,
                availableGisus = persistentListOf(GisuItem(gisuId = 12, generation = 12), otherGisu),
            )))
        }
    }

    @Test
    fun `목록에 없는 기수는 작성 대상으로 보내지 않는다`() {
        val state = state(NoticeWriterRole.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE)
            .copy(targetGisuId = 9)

        assertNull(buildNoticeTargetRequest(state))
    }

    @Test
    fun `대상 기수는 카테고리와 별도로 선택한 기수를 사용한다`() {
        val base = state(NoticeWriterRole.CENTRAL_CORE, WriteCategoryType.GISU)
            .copy(isAllSelected = true, availableGisus = persistentListOf(
                GisuItem(gisuId = 12, generation = 12), GisuItem(gisuId = 9, generation = 9)
            ))

        assertEquals(9, buildNoticeTargetRequest(base.copy(targetGisuId = 9))?.targetGisuId)
    }

    @Test
    fun `서버가 더는 선택받지 않는 과거 파트는 작성 대상으로 보내지 않는다`() {
        val state = state(NoticeWriterRole.SUPER_ADMIN, WriteCategoryType.GISU)
            .copy(selectedPart = UserPart.WEB)

        assertNull(buildNoticeTargetRequest(state))
    }

    private fun state(role: NoticeWriterRole, category: WriteCategoryType) = NoticeWriteUiState(
        writerRole = role,
        selectedCategory = WriteCategory(category, category.name),
        targetGisuId = 12,
        authorityGisuId = 12,
        availableGisus = persistentListOf(GisuItem(gisuId = 12, generation = 12)),
    )
}
