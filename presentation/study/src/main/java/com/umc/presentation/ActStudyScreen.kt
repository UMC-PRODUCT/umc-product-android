package com.umc.presentation.study

import androidx.compose.runtime.Composable
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel
import com.umc.presentation.study.admin.group.AdminStudyGroupRoute
import com.umc.presentation.study.normal.UserStudyRoute

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
    // 관리자는 스터디 그룹 관리 화면을 표시합니다.
    if (isAdmin) {
        AdminStudyGroupRoute(
            isActive = isActive,
            onNavigateCreateGroup = onNavigateCreateGroup,
            onNavigateAddSchedule = onNavigateAddSchedule,
            onOpenEditMembers = onOpenEditMembers,
        )
    } else {
        // 일반 사용자는 본인의 스터디 커리큘럼 화면을 표시합니다.
        UserStudyRoute(isActive = isActive)
    }
}
