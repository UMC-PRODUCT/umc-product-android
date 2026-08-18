package com.umc.presentation.act

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.presentation.study.ActStudyRoute
import com.umc.presentation.study.admin.submit.AdminSubmitRoute
import kotlinx.coroutines.launch

private data class ManageTab(
    val title: String,
    val content: @Composable (isActive: Boolean) -> Unit
)

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
) {
    val tabs = remember(
        uiState.isAdmin,
        onNavigateToChallengerDetail,
        onNavigateCreateStudyGroup,
        onNavigateAddStudySchedule,
        ) {
        if (uiState.isAdmin) {
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
                ManageTab(AppStrings.TAB_STUDY_USER) { UserStudyRoute() },
                ManageTab(AppStrings.TAB_CHALLENGE_USER) { isActive ->
                    NormalChallengerRoute(isActive = isActive)
                }
            )
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tabs.size) {
        if (pagerState.currentPage >= tabs.size) {
            pagerState.scrollToPage(tabs.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
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
                .height(55.dp),
            containerColor = grey000(),
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
                            style = HeadlineBold
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
            tabs[page].content(page == pagerState.currentPage)
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
