package com.umc.presentation.study.admin.group.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.LocalUToast
import com.umc.component.component.UToastState

/**
 * 스터디 그룹 생성 화면의 Route
 *
 * ViewModel과 실제 UI Screen을 연결하는 역할을 합니다.
 *
 * 주요 역할
 * - ViewModel의 UI 상태 구독
 * - ViewModel에서 발생한 일회성 이벤트 처리
 * - 화면에서 발생한 액션을 ViewModel에 전달
 * - 그룹 생성 성공 또는 뒤로가기 시 이전 화면으로 이동
 */
@Composable
fun AdminStudyGroupCreateRoute(
    viewModel: AdminStudyGroupCreateViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    // ViewModel의 현재 화면 상태를 Lifecycle에 맞춰 구독
    val state = viewModel.uiState
        .collectAsStateWithLifecycle()
        .value
    val toast = LocalUToast.current

    /**
     * 현재 생성할 스터디 그룹의 기수는 활성 기수를 따른다.
     */
    LaunchedEffect(Unit) {
        viewModel.loadActiveGisu()
    }

    /**
     * ViewModel에서 전달되는 일회성 UI 이벤트 처리
     *
     * NavigateBack
     * -> 사용자가 뒤로가기 버튼을 클릭한 경우
     *
     * RegisterSuccess
     * -> 그룹 생성 API가 성공한 경우
     *
     * RegisterFailure
     * -> 그룹 생성 API가 실패했거나 기수 정보를 불러오지 못한 경우
     */
    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            AdminStudyGroupCreateEvent.NavigateBack -> {
                navigateBack()
            }

            AdminStudyGroupCreateEvent.RegisterSuccess -> {
                // 그룹 생성 성공 후 이전 화면으로 이동
                navigateBack()
            }

            is AdminStudyGroupCreateEvent.RegisterFailure -> {
                // 화면에 머물며 입력값은 유지한다. 빈 서버 메시지의 기본 문구는 ViewModel 이 채워 보낸다.
                // 파트·파트장·스터디원 시트가 열려 있으면 시트 안의 UToastOverlay 가 그린다.
                toast.show(event.message, UToastState.ERROR)
            }
        }
    }

    /**
     * 실제 스터디 그룹 생성 화면
     *
     * 현재 상태를 전달하고,
     * 화면에서 발생하는 이벤트를 ViewModel의 함수와 연결합니다.
     */
    AdminStudyGroupCreateScreen(
        state = state,

        // 일반적인 사용자 액션 처리
        onAction = viewModel::onAction,

        // 현재 열려 있는 바텀시트 닫기
        onDismissBottomSheet = viewModel::dismissBottomSheet,

        // 파트 선택 결과
        onPartSelected = viewModel::selectPart,

        // 담당 파트장 선택 결과
        onPartLeaderSelected = viewModel::selectPartLeaders,

        // 스터디원 선택 결과
        onMembersSelected = viewModel::selectMembers,
    )
}