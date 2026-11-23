package com.umc.domain.model.notice

import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.NoticeWriterRole
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoticeAccessPropertyTest {
    @Test
    fun `다른 기수의 상위 역할과 역할 순서는 활성 기수의 최고 작성 권한을 바꾸지 않는다`() = runBlocking<Unit> {
        checkAll(200, Arb.long(1L..100000L)) { gisu ->
            val roles = listOf(
                role("CENTRAL_PRESIDENT", gisu + 1, "CENTRAL", null),
                role("SCHOOL_PRESIDENT", gisu, "SCHOOL", 7),
                role("CENTRAL_EDUCATION_TEAM_MEMBER", gisu, "CENTRAL", null),
            )
            val user = UserInfo(roles = roles)
            assertEquals(NoticeWriterRole.CENTRAL_STAFF, user.noticeWriterRole(gisu))
            assertEquals(NoticeWriterRole.CENTRAL_STAFF, user.copy(roles = roles.reversed()).noticeWriterRole(gisu))
            assertNull(user.noticeWriterRole(gisu + 2))
            assertEquals(NoticeWriterRole.SUPER_ADMIN, user.copy(isSuperAdmin = true).noticeWriterRole(gisu + 2))
        }
    }

    @Test
    fun `기존 지부장 교내 회장 겸임의 권한 기수와 소속 ID를 유지한다`() = runBlocking<Unit> {
        checkAll(200, Arb.long(1L..100000L), Arb.long(1L..100000L)) { gisu, school ->
            val user = UserInfo(schoolId = school + 1, roles = listOf(
                role("SCHOOL_PRESIDENT", gisu + 1, "SCHOOL", school + 1),
                role("SCHOOL_PRESIDENT", gisu, "SCHOOL", school),
                role("CHAPTER_PRESIDENT", gisu, "CHAPTER", 5),
            ))
            assertEquals(NoticeWriterRole.CHAPTER_PRESIDENT, user.noticeWriterRole(gisu))
            assertEquals(school, user.noticeWriterSchoolId(gisu))
            assertEquals(5L, user.noticeWriterChapterId(gisu))
        }
    }

    private fun role(type: String, gisu: Long, organization: String, id: Long?) = UserRole(
        id = 1, challengerId = 1, roleType = type, chapterId = 5, chapterName = "지부",
        organizationType = organization, organizationId = id, responsiblePart = null, gisuId = gisu, gisu = gisu,
    )
}
