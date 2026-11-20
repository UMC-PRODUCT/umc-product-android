package com.umc.presentation.study

import androidx.compose.runtime.Composable
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel
import com.umc.presentation.study.admin.group.AdminStudyGroupRoute
import com.umc.presentation.study.normal.UserStudyRoute

/**
 * 활동 스터디 화면의 Route
 *
 * 관리자 여부에 따라 관리자용/일반 사용자용 스터디 Route 로 나눈다.
 * 각 Route 가 자기 ViewModel 과 화면을 맡으므로 여기서는 분기만 한다.
 * 스터디 그룹 생성/일정 등록/멤버 수정 화면 이동 이벤트를 관리자 Route 에 연결한다.
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
    if (isAdmin) {
        // 관리자는 스터디 그룹 관리 화면을 표시합니다.
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
