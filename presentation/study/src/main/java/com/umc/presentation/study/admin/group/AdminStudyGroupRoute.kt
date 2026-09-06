package com.umc.presentation.study.admin.group

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

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
 * - Toast 메시지 출력
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
    val state by viewModel.uiState.collectAsState()

    /**
     * Toast 출력에 사용할 Context
     */
    val context = LocalContext.current

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
             * 사용자에게 Toast 메시지 표시
             */
            is AdminStudyGroupEvent.ShowToast -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT,
                ).show()
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
    )
}