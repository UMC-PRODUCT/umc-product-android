package com.umc.presentation.study.admin.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.LocalUToast
import com.umc.component.component.UToastData

/**
 * 관리자 스터디 그룹 화면의 Route
 *
 * ViewModel과 Screen 사이를 연결합니다.
 *
 * 주요 역할
 * - ViewModel 상태 구독
 * - 화면 활성화 시 그룹 목록 갱신
 * - ViewModel에서 발생한 일회성 Event 처리
 * - 네비게이션 처리
 * - 토스트 메시지 출력
 */
@Composable
fun AdminStudyGroupRoute(
    isActive: Boolean,
    viewModel: AdminStudyGroupViewModel = hiltViewModel(),

    /**
     * 스터디 그룹 생성 화면 이동
     */
    onNavigateCreateGroup: () -> Unit = {},

    /**
     * 스터디 일정 등록 화면 이동
     *
     * 그룹 ID, 그룹 이름, 그룹 파트를 전달합니다.
     */
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },

    /**
     * 외부 멤버 수정 화면을 열 때 사용
     */
    onOpenEditMembers: (
        AdminStudyGroupItemUiModel
    ) -> Unit = {},
) {
    /**
     * ViewModel의 현재 UI 상태 구독
     */
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val toast = LocalUToast.current

    // 다이얼로그는 별도 창이라 앱 토스트가 딤 아래에 가려진다. 다이얼로그가 열린 채로 띄우는 토스트는 다이얼로그 안에 그린다.
    var dialogToast by remember { mutableStateOf<UToastData?>(null) }
    val isDialogOpen = state.isEditDialogOpen || state.isDeleteDialogOpen

    // 다이얼로그가 닫히면 남은 토스트를 버려, 다음에 열 때 지난 토스트가 다시 뜨지 않게 한다.
    LaunchedEffect(isDialogOpen) {
        if (!isDialogOpen) dialogToast = null
    }

    /**
     * 현재 관리자 스터디 화면이 활성화되면
     * 그룹 목록을 다시 조회합니다.
     *
     * 다른 화면에서 그룹을 생성/수정하고 돌아온 경우에도
     * 최신 목록을 확인할 수 있도록 갱신합니다.
     */
    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refreshGroups()
        }
    }

    /**
     * ViewModel에서 발생한 일회성 UI 이벤트 처리
     */
    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {

            /**
             * 그룹 생성 화면 이동
             */
            is AdminStudyGroupEvent.NavigateCreateGroup -> {
                onNavigateCreateGroup()
            }

            /**
             * 선택한 그룹의 일정 등록 화면 이동
             */
            is AdminStudyGroupEvent.NavigateAddSchedule -> {
                onNavigateAddSchedule(
                    event.groupId,
                    event.groupTitle,
                    event.groupPart,
                )
            }

            /**
             * 선택한 그룹의 멤버 수정 화면 열기
             */
            is AdminStudyGroupEvent.OpenEditMembers -> {
                onOpenEditMembers(
                    event.item
                )
            }

            /**
             * 사용자에게 토스트 메시지 표시
             *
             * 수정·삭제 실패는 다이얼로그를 연 채로 오므로, 그때 열려 있는 창에 띄운다.
             */
            is AdminStudyGroupEvent.ShowToast -> {
                val current = viewModel.uiState.value
                if (current.isEditDialogOpen || current.isDeleteDialogOpen) {
                    dialogToast = UToastData(event.message, event.state)
                } else {
                    // 멤버 수정 시트가 열려 있으면 시트 안의 UToastOverlay 가 이 토스트를 그린다.
                    toast.show(event.message, event.state)
                }
            }
        }
    }

    /**
     * 실제 관리자 스터디 그룹 화면
     *
     * ViewModel 상태를 전달하고,
     * 모든 사용자 액션을 ViewModel의 onAction으로 연결합니다.
     */
    AdminStudyGroupScreen(
        state = state,
        onAction = viewModel::onAction,
        dialogToast = dialogToast,
        onDialogToastDismiss = { dialogToast = null },
    )
}