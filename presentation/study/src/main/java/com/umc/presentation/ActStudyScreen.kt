package com.umc.presentation.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel
import com.umc.presentation.study.admin.group.AdminStudyGroupRoute
import com.umc.presentation.study.admin.submit.AdminSubmitRoute
import com.umc.presentation.study.normal.UserStudyRoute
import kotlinx.coroutines.launch

@Composable
fun ActStudyRoute(
    isAdmin: Boolean,
    onNavigateCreateGroup: () -> Unit = {},
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    onOpenEditMembers: (AdminStudyGroupItemUiModel) -> Unit = {},
) {
    ActStudyScreen(
        isAdmin = isAdmin,
        onNavigateCreateGroup = onNavigateCreateGroup,
        onNavigateAddSchedule = onNavigateAddSchedule,
        onOpenEditMembers = onOpenEditMembers,
    )
}

@Composable
fun ActStudyScreen(
    isAdmin: Boolean,
    onNavigateCreateGroup: () -> Unit = {},
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    onOpenEditMembers: (AdminStudyGroupItemUiModel) -> Unit = {},
) {
    if (isAdmin) {
        AdminStudyScreen(
            onNavigateCreateGroup = onNavigateCreateGroup,
            onNavigateAddSchedule = onNavigateAddSchedule,
            onOpenEditMembers = onOpenEditMembers,
        )
    } else {
        UserStudyRoute()
    }
}

@Composable
private fun AdminStudyScreen(
    onNavigateCreateGroup: () -> Unit,
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit,
    onOpenEditMembers: (AdminStudyGroupItemUiModel) -> Unit,
) {
    val tabs = listOf("제출 현황", "스터디 그룹")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(grey100(), RoundedCornerShape(1000.dp))
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (isSelected) 2.dp else 0.dp,
                            shape = RoundedCornerShape(1000.dp),
                        )
                        .background(
                            color = if (isSelected) grey000() else grey100(),
                            shape = RoundedCornerShape(1000.dp),
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        style = HeadlineBold,
                        color = if (isSelected) grey800() else grey400(),
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
                    AdminSubmitRoute()
                }

                1 -> {
                    AdminStudyGroupRoute(
                        onNavigateCreateGroup = onNavigateCreateGroup,
                        onNavigateAddSchedule = onNavigateAddSchedule,
                        onOpenEditMembers = onOpenEditMembers,
                    )
                }
            }
        }
    }
}