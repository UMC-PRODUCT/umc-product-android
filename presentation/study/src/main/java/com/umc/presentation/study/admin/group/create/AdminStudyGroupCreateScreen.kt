package com.umc.presentation.study.admin.group.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreateMemberBottomSheet
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreatePartBottomSheet
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreatePartLeaderBottomSheet
import com.umc.presentation.study.admin.group.create.component.GroupCreateSelectRow
import com.umc.presentation.study.admin.group.create.component.GroupCreateTextField
import com.umc.presentation.study.admin.group.create.component.GroupCreateTopBar

/**
 * 스터디 그룹 생성 화면
 *
 * 그룹 생성에 필요한 정보를 입력하고 선택하는 UI를 구성합니다.
 *
 * 사용자가 입력하는 정보
 * - 그룹 이름
 * - 해당 파트
 * - 담당 파트장
 * - 스터디원
 *
 * 선택형 항목은 각각의 바텀시트를 통해 선택하며,
 * 모든 필수 조건을 만족하면 상단의 등록 버튼이 활성화됩니다.
 *
 * @param state 현재 그룹 생성 화면 상태
 * @param onAction 화면에서 발생한 사용자 액션 전달
 * @param onDismissBottomSheet 현재 열린 바텀시트 닫기
 * @param onPartSelected 파트 선택 결과 전달
 * @param onPartLeaderSelected 담당 파트장 선택 결과 전달
 * @param onMembersSelected 스터디원 선택 결과 전달
 */
@Composable
fun AdminStudyGroupCreateScreen(
    state: AdminStudyGroupCreateState,
    onAction: (AdminStudyGroupCreateAction) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onPartSelected: (AdminStudyGroupCreatePartUiModel) -> Unit,
    onPartLeaderSelected: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
    onMembersSelected: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 20.dp)
    ) {

        /**
         * 화면 상단 영역
         *
         * - 뒤로가기
         * - 화면 제목
         * - 알림
         * - 등록 버튼
         *
         * 필수 입력값이 모두 선택되었을 때만
         * 등록 버튼을 활성화합니다.
         */
        GroupCreateTopBar(
            isRegisterEnabled = state.isRegisterEnabled,

            onBackClick = {
                onAction(
                    AdminStudyGroupCreateAction.OnBackClick
                )
            },

            onRegisterClick = {
                onAction(
                    AdminStudyGroupCreateAction.OnRegisterClick
                )
            },

            onNotificationClick = {
                // TODO 알림 버튼 클릭 시 동작 연결
            }
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /**
         * 그룹 이름 입력
         *
         * 사용자가 입력한 값은
         * OnGroupNameChanged 액션을 통해 ViewModel로 전달합니다.
         */
        GroupCreateTextField(
            title = "그룹 이름",
            value = state.groupName,
            placeholder = "예: React 실습 A팀",
            onValueChange = { value ->
                onAction(
                    AdminStudyGroupCreateAction.OnGroupNameChanged(
                        value = value
                    )
                )
            }
        )

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        /**
         * 파트 선택
         *
         * 현재 선택된 파트가 있으면 label을 표시하고,
         * 클릭하면 파트 선택 바텀시트를 엽니다.
         */
        GroupCreateSelectRow(
            title = "해당 파트",
            value = state.selectedPart
                ?.label
                .orEmpty(),
            placeholder = "파트를 선택하세요",
            onClick = {
                onAction(
                    AdminStudyGroupCreateAction.OnPartClick
                )
            }
        )

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        /**
         * 담당 파트장 선택
         *
         * 선택된 파트장은 partLeaderSummary를 통해
         * 요약된 문자열로 표시합니다.
         *
         * 클릭하면 파트장 검색/선택 바텀시트를 엽니다.
         */
        GroupCreateSelectRow(
            title = "담당 파트장",
            value = state.partLeaderSummary,
            placeholder = "담당 파트장을 선택하세요",
            onClick = {
                onAction(
                    AdminStudyGroupCreateAction.OnPartLeaderClick
                )
            }
        )

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        /**
         * 스터디원 선택
         *
         * 선택된 인원은 memberSummary를 통해
         * "홍길동 외 3명" 형태로 요약하여 표시합니다.
         *
         * 클릭하면 스터디원 검색/선택 바텀시트를 엽니다.
         */
        GroupCreateSelectRow(
            title = "스터디원 추가",
            value = state.memberSummary,
            placeholder = "스터디원을 선택하세요",
            onClick = {
                onAction(
                    AdminStudyGroupCreateAction.OnMemberClick
                )
            }
        )
    }

    /**
     * 파트 선택 바텀시트
     *
     * showPartBottomSheet가 true인 경우에만 표시합니다.
     */
    if (state.showPartBottomSheet) {
        GroupCreatePartBottomSheet(
            selectedPart = state.selectedPart,

            onDismissRequest = onDismissBottomSheet,

            onPartSelected = onPartSelected
        )
    }

    /**
     * 담당 파트장 선택 바텀시트
     *
     * 기존에 선택되어 있는 파트장을 preSelected로 전달하여
     * 바텀시트를 다시 열어도 기존 선택 상태를 유지합니다.
     */
    if (state.showPartLeaderBottomSheet) {
        GroupCreatePartLeaderBottomSheet(
            preSelected = state.selectedPartLeaders,

            onDismissRequest = onDismissBottomSheet,

            onConfirm = onPartLeaderSelected
        )
    }

    /**
     * 스터디원 선택 바텀시트
     *
     * 기존에 선택되어 있는 스터디원을 preSelected로 전달하여
     * 추가/삭제 시 현재 선택 상태를 유지합니다.
     */
    if (state.showMemberBottomSheet) {
        GroupCreateMemberBottomSheet(
            preSelected = state.selectedMembers,

            onDismissRequest = onDismissBottomSheet,

            onConfirm = onMembersSelected
        )
    }
}