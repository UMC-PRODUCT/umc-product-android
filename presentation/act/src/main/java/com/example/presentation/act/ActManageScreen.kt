package com.example.presentation.act

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
import androidx.compose.material3.Switch
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
import com.example.presentation.act.admin.attendance.AttendanceRoute
import com.example.presentation.act.admin.challanger.ChallengerRoute
import com.example.presentation.act.normal.attendance.NormalAttendanceRoute
import com.umc.component.theme.AppStrings
import com.umc.component.theme.AppStrings.ADMIN_LABEL
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title2Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import kotlinx.coroutines.launch

private data class ManageTab(
    val title: String,
    val content: @Composable () -> Unit
)

@Composable
fun ActManageRoute(
    vm: ActViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    ActManageScreen(
        uiState = uiState,
        onAdminCheckedChange = vm::setAdminMode
    )
}

@Composable
private fun ActManageScreen(
    uiState: ActViewModel.ActivityManagementUiState,
    onAdminCheckedChange: (Boolean) -> Unit
) {
    val tabs = remember(uiState.isAdmin) {
        if (uiState.isAdmin) {
            listOf(
                ManageTab(AppStrings.TAB_ATTENDANCE_ADMIN) { AttendanceRoute() },
                ManageTab(AppStrings.TAB_STUDY_ADMIN) { ComingSoonScreen() },
                ManageTab(AppStrings.TAB_CHALLENGE_ADMIN) { ChallengerRoute() }
            )
        } else {
            listOf(
                ManageTab(AppStrings.TAB_ATTENDANCE_USER) { NormalAttendanceRoute() },
                ManageTab(AppStrings.TAB_STUDY_USER) { ComingSoonScreen() },
                ManageTab(AppStrings.TAB_CHALLENGE_USER) { ComingSoonScreen() }
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
            .background(neutral000())
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
            containerColor = neutral000(),
            contentColor = neutral800(),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = neutral800()
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
                                neutral800()
                            } else {
                                neutral400()
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
                .background(neutral100())
        ) { page ->
            tabs[page].content()
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
            color = neutral600()
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
            color = neutral800(),
            style = Title2Bold
        )

        if (hasAdminAccess) {
            Text(
                text = ADMIN_LABEL,
                modifier = Modifier.padding(end = 8.dp),
                color = neutral600(),
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
    Switch(
        checked = isAdmin,
        onCheckedChange = onAdminChanged
    )
}

@Preview(showBackground = true)
@Composable
private fun AdminActScreenPreview() {
    UmcTheme(darkTheme = false) {
        ActManageScreen(
            uiState = ActViewModel.ActivityManagementUiState(
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
            uiState = ActViewModel.ActivityManagementUiState(
                isAdmin = false,
                hasAdminAccess = true
            ),
            onAdminCheckedChange = {}
        )
    }
}