package com.umc.presentation.study.admin.group.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

/**
 * 관리자 스터디 그룹의 스터디원을 수정하는 BottomSheet
 *
 * 현재 그룹에 포함된 스터디원을 확인하고 삭제할 수 있으며,
 * 이름 검색을 통해 새로운 스터디원을 추가할 수 있습니다.
 *
 * 주요 기능
 * - 현재 스터디원 목록 표시
 * - 기존 스터디원 삭제
 * - 이름 기반 챌린저 검색
 * - 검색 결과 파트별 표시
 * - 검색 결과에서 추가할 멤버 임시 선택
 * - 검색 결과 커서 페이지네이션
 * - 변경된 멤버 목록 전달
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudyGroupMemberEditBottomSheet(
    viewModel: AdminStudyGroupMemberEditViewModel = hiltViewModel(),
    preSelected: List<AdminStudyGroupCreateMemberUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * BottomSheet가 열릴 때
     * 현재 스터디 그룹에 포함된 멤버를 ViewModel에 전달합니다.
     */
    LaunchedEffect(preSelected) {
        viewModel.initialize(preSelected)
    }

    /**
     * BottomSheet를 닫습니다.
     *
     * 처음 전달받은 멤버 목록과 현재 멤버 목록이 달라졌다면
     * 변경된 멤버 목록을 상위 화면에 전달한 뒤 상태를 초기화합니다.
     */
    fun dismissBottomSheet() {
        if (state.hasChanges) {
            onConfirm(state.selectedMembers)
        }

        viewModel.reset()
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissBottomSheet,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600(),
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {

            // 상단 제목 및 검색 결과 확인 버튼
            EditMemberHeader(
                isSearching = state.isSearching,
                isConfirmEnabled = state.isConfirmEnabled,
                onConfirmClick = {
                    viewModel.confirmPendingMembers()
                },
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // 스터디원 검색 입력창
            UTextField(
                value = state.query,
                onValueChange = viewModel::searchMembers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(
                    R.drawable.ic_search
                ),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp,
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /**
             * BottomSheet의 멤버 목록 영역
             *
             * 로딩 중      -> 로딩 인디케이터
             * 검색 중      -> 검색 결과
             * 검색하지 않음 -> 현재 스터디원 목록
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(
                                Alignment.Center
                            ),
                            color = indigo500(),
                        )
                    }

                    state.isSearching -> {
                        EditMemberSearchContent(
                            state = state,
                            onToggleMember = {
                                viewModel.togglePendingMember(it)
                            },
                            onLoadMore = {
                                viewModel.loadMoreMembers()
                            },
                        )
                    }

                    else -> {
                        EditCurrentMemberContent(
                            members = state.selectedMembers,
                            onRemoveMember = {
                                viewModel.removeMember(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 멤버 수정 BottomSheet의 상단 영역
 *
 * 일반 상태에서는 "스터디원을 추가하세요"를 표시하고,
 * 검색 중에는 "스터디원을 검색하세요"와 확인 버튼을 표시합니다.
 */
@Composable
private fun EditMemberHeader(
    isSearching: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = if (isSearching) {
                "스터디원을 검색하세요"
            } else {
                "스터디원을 추가하세요"
            },
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f),
        )

        // 검색 중일 때만 확인 버튼 표시
        if (isSearching) {
            UButton(
                text = "확인",
                onClick = onConfirmClick,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp),
                backgroundColor = if (isConfirmEnabled) {
                    indigo500()
                } else {
                    grey100()
                },
                textColor = if (isConfirmEnabled) {
                    grey000()
                } else {
                    grey400()
                },
                textStyle = UmcTypographyTokens.SubheadlineBold,
                cornerRadius = 8.dp,
            )
        }
    }
}

/**
 * 현재 스터디 그룹에 포함된 멤버 목록을 표시합니다.
 *
 * 멤버가 없는 경우 빈 상태 문구를 표시하며,
 * 멤버가 존재하는 경우 삭제 가능한 목록으로 표시합니다.
 */
@Composable
private fun EditCurrentMemberContent(
    members: List<AdminStudyGroupCreateMemberUiModel>,
    onRemoveMember: (AdminStudyGroupCreateMemberUiModel) -> Unit,
) {
    if (members.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            UText(
                text = "추가된 스터디원이 없어요",
                style = UmcTypographyTokens.Body,
                color = grey500(),
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = members,
            key = { member ->
                member.id
            },
        ) { member ->
            EditMemberRow(
                member = member,
                onDeleteClick = {
                    onRemoveMember(member)
                },
            )
        }
    }
}

/**
 * 현재 스터디 그룹에 포함된 개별 멤버 항목
 *
 * 프로필 이미지, 이름, 학교 정보를 표시하고
 * 우측 삭제 버튼을 통해 현재 그룹에서 제거할 수 있습니다.
 */
@Composable
private fun EditMemberRow(
    member: AdminStudyGroupCreateMemberUiModel,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 사용자 프로필 이미지
        AdminStudyMemberProfileImage(
            profileImageUrl = member.profileImageUrl,
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800(),
            )

            // 학교 정보가 존재할 때만 표시
            if (member.school.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                UText(
                    text = member.school,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )
            }
        }

        // 현재 그룹에서 스터디원 삭제
        UButton(
            text = "삭제",
            onClick = onDeleteClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor = red500().copy(
                alpha = 0.12f
            ),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp,
        )
    }
}

/**
 * 챌린저 검색 결과를 표시합니다.
 *
 * 검색 결과는 파트별로 그룹화하여 표시하며,
 * 이미 현재 스터디 그룹에 포함된 멤버는 선택할 수 없습니다.
 *
 * 새롭게 선택한 멤버는 pendingMembers에 임시 저장되고
 * 상단 확인 버튼을 누르면 실제 선택 목록에 추가됩니다.
 */
@Composable
private fun EditMemberSearchContent(
    state: AdminStudyGroupMemberEditState,
    onToggleMember: (
        AdminStudyGroupCreateMemberUiModel
    ) -> Unit,
    onLoadMore: () -> Unit,
) {
    // 검색 결과를 파트별로 그룹화
    val groupedResults = state.searchResults
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        groupedResults.forEach { (partLabel, members) ->

            // 파트명 헤더
            if (partLabel.isNotBlank()) {
                item(
                    key = "header_$partLabel"
                ) {
                    UText(
                        text = partLabel,
                        style = UmcTypographyTokens.BodyBold,
                        color = grey900(),
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 8.dp,
                        ),
                    )
                }
            }

            // 해당 파트의 검색 결과
            items(
                items = members,
                key = { member ->
                    member.id
                },
            ) { member ->

                /**
                 * 이미 현재 그룹에 포함된 멤버인지 확인
                 *
                 * 기존 멤버인 경우 체크 상태로 표시하지만
                 * 다시 선택하거나 해제할 수 없습니다.
                 */
                val isAlreadyMember =
                    state.selectedMembers.any {
                        it.id == member.id
                    }

                /**
                 * 검색 화면에서 새롭게 임시 선택된 멤버인지 확인
                 */
                val isPending =
                    state.pendingMembers.any {
                        it.id == member.id
                    }

                EditSearchMemberRow(
                    member = member,
                    isChecked =
                        isAlreadyMember || isPending,
                    enabled = !isAlreadyMember,
                    onClick = {
                        onToggleMember(member)
                    },
                )
            }

            // 파트 그룹 사이 여백
            item(
                key = "spacing_$partLabel"
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }

        /**
         * 다음 페이지가 존재하는 경우
         * 리스트 끝에 도달했을 때 다음 검색 결과를 조회합니다.
         */
        if (
            state.hasNext &&
            state.searchResults.isNotEmpty() &&
            !state.isLoading
        ) {
            item(
                key = "load_more"
            ) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
    }
}

/**
 * 챌린저 검색 결과의 개별 멤버 항목
 *
 * 프로필 이미지, 이름, 학교 정보를 표시하며
 * 우측 체크박스로 선택 여부를 표시합니다.
 *
 * 이미 현재 그룹에 포함된 멤버는
 * 체크된 상태로 표시되며 선택할 수 없습니다.
 */
@Composable
private fun EditSearchMemberRow(
    member: AdminStudyGroupCreateMemberUiModel,
    isChecked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 사용자 프로필 이미지
        AdminStudyMemberProfileImage(
            profileImageUrl = member.profileImageUrl,
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = if (enabled) {
                    grey800()
                } else {
                    grey500()
                },
            )

            // 학교 정보가 존재할 때만 표시
            if (member.school.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                UText(
                    text = member.school,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )
            }
        }

        // 멤버 선택 상태
        Icon(
            painter = painterResource(
                if (isChecked) {
                    R.drawable.ic_check_box_primary
                } else {
                    R.drawable.ic_check_box_empty
                }
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * 스터디 그룹 멤버의 프로필 이미지를 표시합니다.
 *
 * profileImageUrl이 존재하는 경우 서버의 프로필 이미지를 표시하고,
 * URL이 없거나 비어 있는 경우 기본 프로필 아이콘을 표시합니다.
 */
@Composable
private fun AdminStudyMemberProfileImage(
    profileImageUrl: String?,
) {
    if (!profileImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )
    }
}