package com.umc.presentation.act

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.presentation.act.admin.attendance.AttendanceRoute
import com.umc.presentation.study.normal.UserStudyRoute
import com.umc.presentation.act.admin.challenger.AdminChallengerRoute
import com.umc.presentation.act.normal.attendance.NormalAttendanceRoute
import com.umc.presentation.act.normal.challenger.NormalChallengerRoute
import com.umc.component.component.USwitch
import com.umc.component.theme.AppStrings
import com.umc.component.theme.AppStrings.ADMIN_LABEL
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title2Bold
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.presentation.study.ActStudyRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private data class ManageTab(
    val title: String,
    val content: @Composable (isActive: Boolean) -> Unit
)

// 챌린저 모드의 '스터디/활동' 탭 위치. 운영진 모드 목록이면 -1
private fun List<ManageTab>.indexOfStudyTab(): Int =
    indexOfFirst { it.title == AppStrings.TAB_STUDY_USER }

@Composable
fun ActManageRoute(
    vm: ActViewModel = hiltViewModel(),
    onNavigateToChallengerDetail: (Long) -> Unit = {},
    onNavigateCreateStudyGroup: () -> Unit,
    onNavigateAddStudySchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    // 마이페이지 '나의 스터디'에서 들어왔을 때 스터디 탭을 열어 달라는 일회성 요청
    openStudyTab: Boolean = false,
    onStudyTabOpened: () -> Unit = {},
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.getUserInfo()
    }

    ActManageScreen(
        uiState = uiState,
        onAdminCheckedChange = vm::setAdminMode,
        onNavigateToChallengerDetail = onNavigateToChallengerDetail,
        onNavigateCreateStudyGroup = onNavigateCreateStudyGroup,
        onNavigateAddStudySchedule = onNavigateAddStudySchedule,
        openStudyTab = openStudyTab,
        onStudyTabOpened = onStudyTabOpened,
    )
}

@Composable
fun ActManageScreen(
    uiState: ActUiState,
    onAdminCheckedChange: (Boolean) -> Unit,
    onNavigateToChallengerDetail: (Long) -> Unit = {},
    onNavigateCreateStudyGroup: () -> Unit = {},
    onNavigateAddStudySchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    openStudyTab: Boolean = false,
    onStudyTabOpened: () -> Unit = {},
) {
    // '나의 스터디' 요청을 처리하는 동안에는 운영진 모드여도 챌린저 탭 목록을 써서 운영진 탭 화면의 조회가 먼저 돌지 않게 한다
    val showAdminTabs = uiState.isAdmin && !openStudyTab
    val tabs = remember(
        showAdminTabs,
        onNavigateToChallengerDetail,
        onNavigateCreateStudyGroup,
        onNavigateAddStudySchedule,
        ) {
        if (showAdminTabs) {
            listOf(
                ManageTab(AppStrings.TAB_ATTENDANCE_ADMIN) { isActive ->
                    AttendanceRoute(isActive = isActive)
                },
                ManageTab(AppStrings.TAB_STUDY_ADMIN) { isActive ->
                    ActStudyRoute(
                        isAdmin = true,
                        isActive = isActive,
                        onNavigateCreateGroup = onNavigateCreateStudyGroup,
                        onNavigateAddSchedule = onNavigateAddStudySchedule,
                    )
                },
                ManageTab(AppStrings.TAB_CHALLENGE_ADMIN) { isActive ->
                    AdminChallengerRoute(
                        isActive = isActive,
                        onNavigateToDetail = onNavigateToChallengerDetail,
                    )
                }
            )
        } else {
            listOf(
                ManageTab(AppStrings.TAB_ATTENDANCE_USER) { isActive ->
                    NormalAttendanceRoute(isActive = isActive)
                },
                ManageTab(AppStrings.TAB_STUDY_USER) { isActive ->
                    UserStudyRoute(isActive = isActive)
                },
                ManageTab(AppStrings.TAB_CHALLENGE_USER) { isActive ->
                    NormalChallengerRoute(isActive = isActive)
                }
            )
        }
    }

    val pagerState = rememberPagerState(
        // 마이페이지 '나의 스터디'로 처음 들어오면 출석 탭을 거치지 않고 스터디 탭에서 시작한다
        initialPage = if (openStudyTab) tabs.indexOfStudyTab().coerceAtLeast(0) else 0,
        pageCount = { tabs.size }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tabs.size) {
        if (pagerState.currentPage >= tabs.size) {
            pagerState.scrollToPage(tabs.lastIndex)
        }
    }

    val isAdmin by rememberUpdatedState(uiState.isAdmin)

    // 마이페이지 '나의 스터디'에서 들어오면 챌린저 모드의 '스터디/활동' 탭(나의 커리큘럼)을 연다.
    // 운영진 모드의 스터디 탭은 그룹 관리 화면이라 그때만 챌린저 모드로 돌린다.
    LaunchedEffect(openStudyTab) {
        if (!openStudyTab) return@LaunchedEffect
        try {
            if (isAdmin) onAdminCheckedChange(false)
            // 요청을 처리하는 동안 tabs 는 챌린저 탭 목록이다
            pagerState.scrollToPage(tabs.indexOfStudyTab().coerceAtLeast(0))
            // 모드 전환이 반영된 뒤에 요청을 지워야 운영진 탭 목록으로 잠깐 돌아가지 않는다
            snapshotFlow { isAdmin }.first { !it }
        } finally {
            // 사용자 스와이프 등으로 스크롤이 취소돼도 신호를 지워야, 나중에 하단 탭으로 복원될 때 스터디 탭이 다시 열리지 않는다
            onStudyTabOpened()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50())
    ) {
        ActHeader(
            hasAdminAccess = uiState.hasAdminAccess,
            isAdmin = uiState.isAdmin,
            onAdminCheckedChange = onAdminCheckedChange
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 55.dp),
            containerColor = grey50(),
            contentColor = grey800(),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = grey800()
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = tab.title,
                            color = if (pagerState.currentPage == index) {
                                grey800()
                            } else {
                                grey400()
                            },
                            style = HeadlineBold,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(grey100())
        ) { page ->
            // '나의 스터디' 요청을 처리하는 동안에는 복원된 다른 탭이 조회를 시작하지 않도록 스터디 탭만 활성으로 넘긴다
            val isActive = page == pagerState.currentPage &&
                (!openStudyTab || page == tabs.indexOfStudyTab())
            tabs[page].content(isActive)
        }
    }
}

@Composable
private fun ComingSoonScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "준비 중인 화면입니다.",
            style = Subheadline,
            color = grey600()
        )
    }
}

@Composable
private fun ActHeader(
    hasAdminAccess: Boolean,
    isAdmin: Boolean,
    onAdminCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = AppStrings.ACTIVITY_MANAGEMENT_TITLE,
            modifier = Modifier.weight(1f),
            color = grey800(),
            style = Title2Bold
        )

        if (hasAdminAccess) {
            Text(
                text = ADMIN_LABEL,
                modifier = Modifier.padding(end = 8.dp),
                color = grey600(),
                style = Subheadline
            )

            Spacer(modifier = Modifier.width(16.dp))

            AdminToggle(
                isAdmin = isAdmin,
                onAdminChanged = onAdminCheckedChange
            )
        }
    }
}

@Composable
fun AdminToggle(
    isAdmin: Boolean,
    onAdminChanged: (Boolean) -> Unit
) {
    USwitch(
        checked = isAdmin,
        onCheckedChange = onAdminChanged
    )
}

@Preview(showBackground = true)
@Composable
private fun AdminActScreenPreview() {
    UmcTheme(darkTheme = false) {
        ActManageScreen(
            uiState = ActUiState(
                isAdmin = true,
                hasAdminAccess = true
            ),
            onAdminCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NormalActScreenPreview() {
    UmcTheme(darkTheme = false) {
        ActManageScreen(
            uiState = ActUiState(
                isAdmin = false,
                hasAdminAccess = true
            ),
            onAdminCheckedChange = {}
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun NormalActScreenLargeFontPreview() {
    UmcTheme(darkTheme = false) {
        ActManageScreen(
            uiState = ActUiState(
                isAdmin = false,
                hasAdminAccess = true
            ),
            onAdminCheckedChange = {}
        )
    }
}
