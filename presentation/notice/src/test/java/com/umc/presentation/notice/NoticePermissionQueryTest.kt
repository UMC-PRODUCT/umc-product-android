package com.umc.presentation.notice

import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.GisuItem
import com.umc.presentation.notice.adminnotice.AdminNoticeUiState
import com.umc.presentation.notice.adminnotice.resetAdminNoticeGisu
import com.umc.presentation.notice.adminnotice.resolveAdminNoticeAccess
import com.umc.presentation.notice.adminnotice.AdminNoticeTab
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
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

    @Test
    fun `자동 기수 변경은 이전 파트와 페이징 조건을 해제한다`() {
        val before = NoticeUiState(selectedGisu = 10, selectedPart = UserPart.WEB,
            selectedChipText = UserPart.WEB.label, currentPage = 3, isLastPage = true)
        val after = resolveNoticeGisuOptions(before, UserInfo(), listOf(GisuItem(11, 11, true)))

        assertEquals(11L, after.selectedGisu)
        assertEquals(AppStrings.COMMON_ALL, after.selectedChipText)
        assertNull(after.selectedPart)
        assertEquals(0, after.currentPage)
        assertFalse(after.isLastPage)
    }

    @Test
    fun `동일 기수의 옵션 갱신은 선택 파트와 페이징을 보존한다`() {
        val selected = GisuItem(12, 12, true)
        val before = NoticeUiState(selectedGisu = 12, selectedPart = UserPart.PLAN,
            selectedChipText = UserPart.PLAN.label, currentPage = 3, isLastPage = true)
        val after = resolveNoticeGisuOptions(before, UserInfo(), listOf(GisuItem(13, 13), selected))

        assertEquals(12L, after.selectedGisu)
        assertEquals(UserPart.PLAN, after.selectedPart)
        assertEquals(UserPart.PLAN.label, after.selectedChipText)
        assertEquals(3, after.currentPage)
        assertTrue(after.isLastPage)
    }

    @Test
    fun `기수 옵션이 없어지면 파트와 선택 기수도 해제한다`() {
        val before = NoticeUiState(selectedGisu = 12, selectedPart = UserPart.PLAN,
            selectedChipText = UserPart.PLAN.label)
        val after = resolveNoticeGisuOptions(before, UserInfo(), emptyList())

        assertEquals(0L, after.selectedGisu)
        assertEquals(AppStrings.COMMON_ALL, after.selectedChipText)
        assertNull(after.selectedPart)
        assertFalse(after.canOpenAdminNotice)
    }

    @Test
    fun `같은 권한의 프로필 갱신은 조회 완료와 페이징 및 읽음 상태를 보존한다`() {
        val user = UserInfo(roles = listOf(role(UserChallengerRole.SCHOOL_PART_LEADER, 12)))
        val before = resolveAdminNoticeAccess(AdminNoticeUiState(gisuId = 12), user).copy(
            hasLoaded = true, currentPage = 3, isLastPage = true, isPageLoading = true,
            readNoticeIds = persistentSetOf(1L, 2L), isNextPageError = true, errorMessage = "페이지 실패",
        )
        val after = resolveAdminNoticeAccess(before, user.copy(name = "갱신된 이름"))

        assertEquals(before, after)
        assertTrue(after.hasLoaded)
    }

    @Test
    fun `최초 권한 확정과 권한 변경은 조회가 필요하고 읽음 상태는 보존한다`() {
        val user = UserInfo(roles = listOf(role(UserChallengerRole.SCHOOL_PART_LEADER, 12)))
        val first = resolveAdminNoticeAccess(AdminNoticeUiState(gisuId = 12), user)
        assertTrue(first.canQuery)
        assertFalse(first.hasLoaded)
        val before = first.copy(hasLoaded = true, currentPage = 3, readNoticeIds = persistentSetOf(1L))
        val changed = resolveAdminNoticeAccess(before, UserInfo(isSuperAdmin = true))

        assertTrue(changed.canQuery)
        assertFalse(changed.hasLoaded)
        assertEquals(0, changed.currentPage)
        assertEquals(AdminNoticeTab.PART_LEADER, changed.selectedTab)
        assertTrue(AdminNoticeTab.CENTRAL in changed.visibleTabs)
        assertEquals(before.readNoticeIds, changed.readNoticeIds)
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
