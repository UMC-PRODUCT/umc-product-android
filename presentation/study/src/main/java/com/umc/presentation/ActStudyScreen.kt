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
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.launch

/**
 * 활동 스터디 화면의 Route
 *
 * 관리자 여부와 현재 화면 활성 상태를 전달하고,
 * 스터디 그룹 생성/일정 등록/멤버 수정 화면 이동 이벤트를 연결합니다.
 */
@Composable
fun ActStudyRoute(
    isAdmin: Boolean,
    isActive: Boolean,
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
        isActive = isActive,
        onNavigateCreateGroup = onNavigateCreateGroup,
        onNavigateAddSchedule = onNavigateAddSchedule,
        onOpenEditMembers = onOpenEditMembers,
    )
}

/**
 * 관리자 여부에 따라 관리자용/일반 사용자용 스터디 화면을 분기합니다.
 */
@Composable
fun ActStudyScreen(
    isAdmin: Boolean,
    isActive: Boolean,
    onNavigateCreateGroup: () -> Unit = {},
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    onOpenEditMembers: (AdminStudyGroupItemUiModel) -> Unit = {},
) {
    // 관리자는 제출 현황 및 스터디 그룹 관리 화면 표시
    if (isAdmin) {
        AdminStudyScreen(
            isActive = isActive,
            onNavigateCreateGroup = onNavigateCreateGroup,
            onNavigateAddSchedule = onNavigateAddSchedule,
            onOpenEditMembers = onOpenEditMembers,
        )
    } else {
        // 일반 사용자는 본인의 스터디 진행 화면 표시
        UserStudyRoute()
    }
}

/**
 * 관리자용 스터디 화면
 *
 * 제출 현황과 스터디 그룹 관리 화면을 탭으로 구분하여 표시합니다.
 */
@Composable
private fun AdminStudyScreen(
    isActive: Boolean,
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
        // 관리자 화면 상단 탭
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(grey100(), RoundedCornerShape(1000.dp))
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index

                // 선택된 탭은 흰색 배경과 그림자로 강조
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

        // 탭 선택에 따라 제출 현황 또는 스터디 그룹 관리 화면 표시
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
                    // 제출 현황 탭이 실제로 보일 때만 내부 화면 활성화
                    AdminSubmitRoute(
                        isActive = isActive && pagerState.currentPage == 0,
                    )
                }

                1 -> {
                    // 스터디 그룹 탭이 실제로 보일 때만 내부 화면 활성화
                    AdminStudyGroupRoute(
                        isActive = isActive && pagerState.currentPage == 1,
                        onNavigateCreateGroup = onNavigateCreateGroup,
                        onNavigateAddSchedule = onNavigateAddSchedule,
                        onOpenEditMembers = onOpenEditMembers,
                    )
                }
            }
        }
    }
}