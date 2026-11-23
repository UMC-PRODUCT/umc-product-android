package com.umc.presentation.notice

import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.notice.adminnotice.AdminNoticeUiState
import com.umc.presentation.notice.adminnotice.resetAdminNoticeGisu
import com.umc.presentation.notice.adminnotice.resolveAdminNoticeAccess
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

    @Test
    fun `권한을 잃으면 탭 선택과 검색 및 페이징 준비 상태를 비운다`() {
        val before = AdminNoticeUiState(gisuId = 12, hasAccess = true,
            visibleTabs = persistentListOf(AdminNoticeTab.PART_LEADER), selectedTab = AdminNoticeTab.PART_LEADER,
            currentPage = 3, isPageLoading = true, hasLoaded = true)
        val after = resolveAdminNoticeAccess(before, UserInfo())
        assertFalse(after.hasAccess)
        assertFalse(after.canQuery)
        assertTrue(after.visibleTabs.isEmpty())
        assertNull(after.selectedTab)
        assertEquals(0, after.currentPage)
        assertFalse(after.isPageLoading)
    }

    @Test
    fun `유효 기수와 권한 및 허용 탭이 모두 있어야 조회할 수 있다`() {
        val user = UserInfo(roles = listOf(role(UserChallengerRole.SCHOOL_PART_LEADER, 12)))
        val allowed = resolveAdminNoticeAccess(AdminNoticeUiState(gisuId = 12), user)
        assertTrue(allowed.canQuery)
        assertFalse(allowed.copy(gisuId = 0).canQuery)
        assertFalse(allowed.copy(selectedTab = AdminNoticeTab.CENTRAL).canQuery)
        assertFalse(resolveAdminNoticeAccess(allowed.copy(gisuId = 13), user).canQuery)
        assertFalse(resolveAdminNoticeAccess(AdminNoticeUiState(), UserInfo(isSuperAdmin = true)).canQuery)
    }

    @Test
    fun `권한 확인 전과 nav 기수 도착 전에는 거부를 확정하거나 조회하지 않는다`() {
        val user = UserInfo(roles = listOf(role(UserChallengerRole.SCHOOL_PART_LEADER, 12)))
        val initial = AdminNoticeUiState()
        assertFalse(initial.isAccessResolved)
        assertFalse(initial.canQuery)
        assertFalse(resolveAdminNoticeAccess(initial, user).isAccessResolved)
        val pending = resetAdminNoticeGisu(initial, 12)
        assertFalse(pending.isAccessResolved)
        assertFalse(pending.canQuery)
        val allowed = resolveAdminNoticeAccess(pending, user)
        assertTrue(allowed.isAccessResolved)
        assertTrue(allowed.canQuery)
        val denied = resolveAdminNoticeAccess(pending, UserInfo())
        assertTrue(denied.isAccessResolved)
        assertFalse(denied.hasAccess)
        assertFalse(denied.canQuery)
    }

    @Test
    fun `기수 초기화는 프로필 캐시 조회 전 페이지 오류와 권한 및 선택을 모두 비운다`() {
        val before = AdminNoticeUiState(gisuId = 12, hasAccess = true, isAccessResolved = true,
            visibleTabs = persistentListOf(AdminNoticeTab.PART_LEADER), selectedTab = AdminNoticeTab.PART_LEADER,
            currentPage = 3, isPageLoading = true, isLastPage = true, hasLoaded = true,
            isNextPageError = true, errorMessage = "다음 페이지 실패")
        val pending = resetAdminNoticeGisu(before, 13)
        assertFalse(pending.isAccessResolved)
        assertFalse(pending.hasAccess)
        assertFalse(pending.canQuery)
        assertFalse(pending.isNextPageError)
        assertFalse(pending.isPageLoading)
        assertFalse(pending.isLastPage)
        assertFalse(pending.hasLoaded)
        assertEquals(0, pending.currentPage)
        assertNull(pending.errorMessage)
        assertNull(pending.selectedTab)
        assertTrue(pending.visibleTabs.isEmpty())
        val cached = UserInfo(roles = listOf(role(UserChallengerRole.SCHOOL_PART_LEADER, 13)))
        assertTrue(resolveAdminNoticeAccess(pending, cached).canQuery)
        assertFalse(resolveAdminNoticeAccess(pending, cached).isNextPageError)
    }

    @Test
    fun `잘못된 nav 기수는 조회 없이 종료 상태로 확정한다`() {
        val invalid = resetAdminNoticeGisu(AdminNoticeUiState(), 0)
        assertTrue(invalid.isAccessResolved)
        assertFalse(invalid.canQuery)
        assertTrue(resolveAdminNoticeAccess(invalid, UserInfo(isSuperAdmin = true)).isAccessResolved)
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
