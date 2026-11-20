package com.umc.presentation.notice

import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.notice.adminnotice.AdminNoticeTab
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticePermissionQueryTest {
    @Test
    fun `운영진 공지 아이콘은 선택 기수에 운영진 역할이 있을 때만 보인다`() {
        val user = UserInfo(roles = listOf(
            role(UserChallengerRole.CHALLENGER, 12),
            role(UserChallengerRole.SCHOOL_PART_LEADER, 13),
        ))

        assertFalse(user.canOpenAdminNotice(12))
        assertTrue(user.canOpenAdminNotice(13))
        assertFalse(user.canOpenAdminNotice(0))
        assertTrue(UserInfo(isSuperAdmin = true).canOpenAdminNotice(12))
    }

    @Test
    fun `지부장은 중앙 탭 없이 학교 회장단 탭부터 조회한다`() {
        assertEquals(AdminNoticeTab.SCHOOL_CORE,
            AdminNoticeTab.highestAccessibleTab(listOf(UserChallengerRole.CHAPTER_PRESIDENT)))
    }

    @Test
    fun `교내 기타 운영진은 파트장 탭부터 조회한다`() {
        assertEquals(AdminNoticeTab.PART_LEADER,
            AdminNoticeTab.highestAccessibleTab(listOf(UserChallengerRole.SCHOOL_ETC_ADMIN)))
    }

    @Test
    fun `운영진 검색 쿼리는 학교를 고정하지 않는다`() {
        val state = NoticeUiState(
            selectedGisu = 12,
            chipList = persistentListOf(
                NoticeChipState(text = AppStrings.COMMON_ALL),
                NoticeChipState(text = AppStrings.NOTICE_CENTRAL_CHIP, isClicked = true, isStaffNoticeChip = true),
            ),
        )

        val query = buildNoticeQuery(state, "SCHOOL_CORE")

        assertEquals("SCHOOL_CORE", query.noticeTab)
        assertNull(query.schoolId)
        assertNull(query.chapterId)
    }

    private fun role(type: UserChallengerRole, gisuId: Long) = UserRole(
        id = 1,
        challengerId = 1,
        roleType = type.name,
        chapterId = null,
        chapterName = null,
        organizationType = "SCHOOL",
        organizationId = null,
        responsiblePart = null,
        gisuId = gisuId,
        gisu = gisuId,
    )
}
