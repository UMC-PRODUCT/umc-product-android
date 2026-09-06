package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

/**
 * 스터디 그룹 생성 시 담당 파트장을 선택하는 BottomSheet
 *
 * 주요 기능
 * - 기존 선택 파트장 표시
 * - 이름 기반 파트장 검색
 * - 검색 결과를 파트별로 구분하여 표시
 * - 검색 결과에서 파트장 선택
 * - 기존 파트장 삭제
 * - 사용자 프로필 이미지 표시
 *
 * 멤버 프로필 이미지는 공통 컴포넌트인
 * GroupCreateAddedMemberRow와 GroupCreateMultiSearchRow에서 처리합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreatePartLeaderBottomSheet(
    viewModel: GroupCreateMemberPickerViewModel = hiltViewModel(),
    preSelected: ImmutableList<AdminStudyGroupCreateMemberUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    /**
     * 바텀시트가 닫힐 때 현재 선택된 파트장 목록을
     * 상위 화면으로 전달한 후 내부 상태를 초기화합니다.
     */
    fun dismissWithApply() {
        onConfirm(state.selectedMembers)
        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    /**
     * 바텀시트가 처음 열리거나
     * 기존 선택 파트장이 변경되면 선택 상태를 초기화합니다.
     */
    LaunchedEffect(preSelected) {
        viewModel.resetAfterDismiss()
        viewModel.setSelected(preSelected)
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600()
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // 검색 여부에 따라 제목 변경
            GroupCreatePickerTitle(
                title = if (state.isSearching) {
                    "담당 파트장을 검색하세요"
                } else {
                    "담당 파트장을 추가하세요"
                }
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // 파트장 이름 검색창
            UTextField(
                value = state.query,
                onValueChange = viewModel::searchMembers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(
                    id = R.drawable.ic_search
                ),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    // 검색 중인 경우 검색 결과 표시
                    state.isSearching -> {
                        GroupCreatePartLeaderSearchResults(
                            searchResults = state.searchResults,
                            selectedMembers = state.selectedMembers,
                            onSelectClick = { member ->

                                // 선택한 멤버를 파트장 목록에 추가
                                viewModel.setSelected(
                                    (
                                            state.selectedMembers +
                                                    member
                                            )
                                        .distinctBy { selectedMember ->
                                            selectedMember.id
                                        }
                                )

                                // 선택 완료 후 검색 화면 종료
                                viewModel.clearSearchOnly()
                            }
                        )
                    }

                    // 선택된 파트장이 없는 경우
                    state.selectedMembers.isEmpty() -> {
                        GroupCreateEmptyContent(
                            text = "아직 추가한 파트장이 없어요"
                        )
                    }

                    // 현재 선택된 파트장 목록
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.selectedMembers,
                                key = { member ->
                                    member.id
                                }
                            ) { item ->
                                GroupCreateAddedMemberRow(
                                    item = item,
                                    onRemoveClick = {
                                        viewModel.removeMember(item)
                                    }
                                )
                            }
                        }
                    }
                }

                // 검색 API 로딩 표시
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        ),
                        color = indigo500()
                    )
                }
            }
        }
    }
}

/**
 * 담당 파트장 검색 결과를 표시합니다.
 *
 * 검색 결과를 파트별로 그룹화하여 표시하며,
 * 이미 선택되어 있는 사용자는 다시 선택할 수 없습니다.
 *
 * 사용자 프로필 이미지는 GroupCreateMultiSearchRow에서
 * item.profileImageUrl을 통해 표시합니다.
 */
@Composable
private fun GroupCreatePartLeaderSearchResults(
    searchResults: ImmutableList<AdminStudyGroupCreateMemberUiModel>,
    selectedMembers: ImmutableList<AdminStudyGroupCreateMemberUiModel>,
    onSelectClick: (AdminStudyGroupCreateMemberUiModel) -> Unit,
) {
    val groupedResults = searchResults
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groupedResults.forEach { (partLabel, members) ->

            // 파트별 검색 결과 헤더
            if (partLabel.isNotBlank()) {
                item(
                    key = "part_header_$partLabel"
                ) {
                    UText(
                        text = partLabel,
                        style = UmcTypographyTokens.BodyBold,
                        color = grey900(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp,
                                bottom = 8.dp
                            )
                    )
                }
            }

            // 해당 파트의 사용자 목록
            items(
                items = members,
                key = { member ->
                    member.id
                }
            ) { member ->
                val isAlreadySelected =
                    selectedMembers.any { selectedMember ->
                        selectedMember.id == member.id
                    }

                GroupCreateMultiSearchRow(
                    item = member,
                    isChecked = isAlreadySelected,
                    enabled = !isAlreadySelected,
                    onToggleClick = {
                        onSelectClick(member)
                    }
                )
            }

            // 파트 사이 여백
            item(
                key = "part_spacing_$partLabel"
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }
}