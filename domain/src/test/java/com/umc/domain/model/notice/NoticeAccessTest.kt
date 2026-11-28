package com.umc.domain.model.notice

import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserChallengerRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeAccessTest {
    @Test
    fun `선택 기수의 역할만 공지 권한에 사용한다`() {
        val user = UserInfo(roles = listOf(
            role(UserChallengerRole.CENTRAL_PRESIDENT, 10),
            role(UserChallengerRole.SCHOOL_ETC_ADMIN, 11),
        ))

        assertEquals(listOf(UserChallengerRole.SCHOOL_ETC_ADMIN), user.noticeRolesForGisu(11))
        assertEquals(NoticeWriterRole.SCHOOL_ETC_ADMIN, user.noticeWriterRole(11))
        assertNull(user.noticeWriterRole(12))
    }

    @Test
    fun `시스템 관리자 권한은 챌린저 기록 없이 유효하다`() {
        assertEquals(NoticeWriterRole.SUPER_ADMIN, UserInfo(isSuperAdmin = true).noticeWriterRole(12))
    }

    @Test
    fun `총괄단과 일반 중앙 운영진을 구분한다`() {
        assertEquals(NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.from(listOf(UserChallengerRole.CENTRAL_VICE_PRESIDENT)))
        assertEquals(NoticeWriterRole.CENTRAL_STAFF, NoticeWriterRole.from(listOf(UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER)))
    }

    @Test
    fun `학교 대상 작성은 활성 기수 학교 역할의 소속을 사용한다`() {
        val user = UserInfo(
            schoolId = 99,
            roles = listOf(
                role(UserChallengerRole.SCHOOL_PRESIDENT, 9).copy(organizationType = "SCHOOL", organizationId = 5),
                role(UserChallengerRole.SCHOOL_PRESIDENT, 10).copy(organizationType = "SCHOOL", organizationId = 7),
            ),
        )

        assertEquals(7L, user.noticeWriterSchoolId(10))
    }

    @Test
    fun `v2 응답의 지부장 역할은 학교 ID가 아니라 역할의 지부 ID를 사용한다`() {
        // v2 매퍼는 모든 역할을 SCHOOL 소속, organizationId = 회원 학교 ID로 복원한다.
        val user = UserInfo(
            schoolId = 99,
            roles = listOf(
                role(UserChallengerRole.SCHOOL_PRESIDENT, 11).copy(organizationType = "SCHOOL", organizationId = 99, chapterId = 5),
                role(UserChallengerRole.CHAPTER_PRESIDENT, 11).copy(organizationType = "SCHOOL", organizationId = 99, chapterId = 5),
            ),
        )

        assertEquals(5L, user.noticeWriterChapterId(11))
    }

    @Test
    fun `v1 응답의 지부장 역할은 CHAPTER 소속 ID를 사용한다`() {
        val user = UserInfo(
            roles = listOf(
                role(UserChallengerRole.CHAPTER_PRESIDENT, 11).copy(organizationType = "CHAPTER", organizationId = 8, chapterId = 5),
            ),
        )

        assertEquals(8L, user.noticeWriterChapterId(11))
    }

    @Test
    fun `권한 기수에 지부장 역할이 없으면 지부 ID가 없다`() {
        val user = UserInfo(
            roles = listOf(
                role(UserChallengerRole.CHAPTER_PRESIDENT, 10).copy(organizationType = "CHAPTER", organizationId = 8),
                role(UserChallengerRole.SCHOOL_PRESIDENT, 11).copy(organizationType = "SCHOOL", organizationId = 99, chapterId = 5),
            ),
        )

        assertNull(user.noticeWriterChapterId(11))
    }

    @Test
    fun `첫 학교 역할 소속이 없으면 같은 기수의 다음 유효 역할을 먼저 사용한다`() {
        listOf(null, 0L, -1L).forEach { invalidId ->
            val user = UserInfo(schoolId = 99, roles = listOf(
                role(UserChallengerRole.SCHOOL_PRESIDENT, 11).copy(organizationType = "SCHOOL", organizationId = invalidId),
                role(UserChallengerRole.SCHOOL_PART_LEADER, 10).copy(organizationType = "SCHOOL", organizationId = 8),
                role(UserChallengerRole.SCHOOL_PART_LEADER, 11).copy(organizationType = "SCHOOL", organizationId = 7),
            ))
            assertEquals(7L, user.noticeWriterSchoolId(11))
        }
    }

    @Test
    fun `유효한 학교 역할 소속이 없을 때 기존 프로필 학교 폴백을 유지한다`() {
        val user = UserInfo(schoolId = 99, roles = listOf(
            role(UserChallengerRole.SCHOOL_PRESIDENT, 11).copy(organizationId = 0),
            role(UserChallengerRole.SCHOOL_PART_LEADER, 11).copy(organizationId = null),
        ))
        assertEquals(99L, user.noticeWriterSchoolId(11))
        assertNull(user.copy(schoolId = 0).noticeWriterSchoolId(11))
    }

    @Test
    fun `CHAPTER 소속 ID가 없으면 같은 역할의 지부 ID를 사용한다`() {
        listOf(null, 0L, -1L).forEach { invalidId ->
            val user = UserInfo(roles = listOf(
                role(UserChallengerRole.CHAPTER_PRESIDENT, 11).copy(
                    organizationType = "CHAPTER", organizationId = invalidId, chapterId = 5),
            ))
            assertEquals(5L, user.noticeWriterChapterId(11))
        }
    }

    @Test
    fun `첫 지부장 소속이 무효여도 다음 유효 지부장 역할을 탐색한다`() {
        val user = UserInfo(roles = listOf(
            role(UserChallengerRole.CHAPTER_PRESIDENT, 11).copy(
                organizationType = "CHAPTER", organizationId = 0, chapterId = null),
            role(UserChallengerRole.CHAPTER_PRESIDENT, 10).copy(
                organizationType = "CHAPTER", organizationId = 8, chapterId = 8),
            role(UserChallengerRole.CHAPTER_PRESIDENT, 11).copy(
                organizationType = "SCHOOL", organizationId = 99, chapterId = 5),
        ))
        assertEquals(5L, user.noticeWriterChapterId(11))
    }

    @Test
    fun `학교 역할이라도 다른 조직 타입의 ID를 학교 ID로 사용하지 않는다`() {
        val user = UserInfo(schoolId = 99, roles = listOf(
            role(UserChallengerRole.SCHOOL_PRESIDENT, 12).copy(organizationType = "CHAPTER", organizationId = 8),
            role(UserChallengerRole.SCHOOL_PART_LEADER, 12).copy(organizationType = "SCHOOL", organizationId = 7),
        ))
        assertEquals(7L, user.noticeWriterSchoolId(12))
        assertEquals(99L, user.copy(roles = user.roles.take(1)).noticeWriterSchoolId(12))
        assertNull(user.copy(schoolId = 0, roles = user.roles.take(1)).noticeWriterSchoolId(12))
    }

    @Test
    fun `지부 이름은 최종 ID와 일치하는 역할에서만 복원한다`() {
        val first = role(UserChallengerRole.CHAPTER_PRESIDENT, 12).copy(
            organizationType = "CHAPTER", organizationId = 8, chapterId = 5, chapterName = "다른 지부",
        )
        val second = first.copy(organizationId = 8, chapterId = 8, chapterName = "담당 지부")
        val user = UserInfo(roles = listOf(first, second))
        assertEquals(8L, user.noticeWriterChapterId(12))
        assertEquals("담당 지부", user.noticeWriterChapterName(12))
        assertNull(user.copy(roles = listOf(first)).noticeWriterChapterName(12))
    }

    @Test
    fun `v1 지부 ID가 조직에만 있어도 충돌하지 않는 담당 지부명은 복원한다`() {
        val role = role(UserChallengerRole.CHAPTER_PRESIDENT, 12).copy(
            organizationType = "CHAPTER", organizationId = 8, chapterId = null, chapterName = "담당 지부",
        )
        val user = UserInfo(roles = listOf(role))
        assertEquals(8L, user.noticeWriterChapterId(12))
        assertEquals("담당 지부", user.noticeWriterChapterName(12))
        assertNull(user.copy(roles = listOf(role.copy(chapterId = 5))).noticeWriterChapterName(12))
        assertNull(user.copy(roles = listOf(role.copy(chapterName = " "))).noticeWriterChapterName(12))
    }

    private fun role(type: UserChallengerRole, gisuId: Long) = UserRole(
        id = 1,
        challengerId = 1,
        roleType = type.name,
        chapterId = null,
        chapterName = null,
        organizationType = "CENTRAL",
        organizationId = null,
        responsiblePart = null,
        gisuId = gisuId,
        gisu = gisuId,
    )
}
