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
