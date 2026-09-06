package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.collections.immutable.ImmutableList
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

/**
 * 스터디 그룹 생성 및 수정 화면에서
 * 스터디원을 선택하는 BottomSheet
 *
 * 주요 기능
 * - 기존 스터디원 목록 표시
 * - 이름 기반 챌린저 검색
 * - 검색 결과 임시 선택
 * - 확인 버튼을 통한 멤버 추가
 * - 기존 멤버 삭제
 * - 검색 결과 페이지네이션
 * - 사용자 프로필 이미지 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateMemberBottomSheet(
    viewModel: GroupCreateMemberPickerViewModel = hiltViewModel(),
    preSelected: ImmutableList<AdminStudyGroupCreateMemberUiModel>,
    resolvePreSelectedFromApi: Boolean = false,
    onDismissRequest: () -> Unit,
    onConfirm: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * 바텀시트를 완전히 닫을 때
     * 현재 최종 선택된 스터디원 목록을 상위 화면에 전달합니다.
     *
     * 검색 화면에서 아직 확인하지 않은 pendingMembers는
     * 최종 목록에 반영되지 않습니다.
     */
    fun dismissWithApply() {
        onConfirm(state.selectedMembers)
        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    /**
     * 바텀시트가 열릴 때 기존 선택 멤버를 초기화합니다.
     *
     * resolvePreSelectedFromApi가 true인 경우에는
     * 기존 memberId를 기준으로 API에서 사용자 정보를 다시 조회합니다.
     */
    LaunchedEffect(
        preSelected,
        resolvePreSelectedFromApi,
    ) {
        if (resolvePreSelectedFromApi) {
            viewModel.loadSelectedMembers(
                memberIds = preSelected.map { member ->
                    member.id
                }
            )
        } else {
            viewModel.setSelected(preSelected)
        }
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
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
            // 검색 여부에 따라 제목 및 확인 버튼 표시
            GroupCreatePickerHeader(
                title = if (state.isSearching) {
                    "스터디원을 검색하세요"
                } else {
                    "스터디원을 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.isConfirmEnabled,
                onConfirmClick = {
                    viewModel.confirmPendingMembers()
                }
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // 멤버 이름 검색창
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
                modifier = Modifier.height(28.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isSearching) {
                    // 검색 중에는 검색 결과 표시
                    MemberSearchResultContent(
                        state = state,
                        onToggleMember = viewModel::togglePendingMember,
                        onLoadMore = viewModel::loadMoreMembers,
                    )
                } else {
                    // 검색 중이 아니면 현재 선택된 멤버 표시
                    CurrentMemberContent(
                        members = state.selectedMembers,
                        onRemoveMember = viewModel::removeMember,
                    )
                }

                // API 로딩 표시
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
 * 선택된 멤버가 존재하지 않을 때 표시하는 빈 화면
 */
@Composable
fun GroupCreateEmptyContent(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_people
            ),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = grey400()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        UText(
            text = text,
            style = UmcTypographyTokens.Body,
            color = grey600()
        )
    }
}

/**
 * 현재 추가되어 있는 멤버를 표시하는 Row
 *
 * 프로필 이미지, 이름, 학교 정보를 표시하며
 * 삭제 버튼을 통해 현재 목록에서 제거할 수 있습니다.
 */
@Composable
fun GroupCreateAddedMemberRow(
    item: AdminStudyGroupCreateMemberUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사용자 프로필 이미지
        GroupCreateMemberProfile(
            profileImageUrl = item.profileImageUrl,
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800()
            )
        }

        // 멤버 삭제 버튼
        UButton(
            text = "삭제",
            onClick = onRemoveClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor = red500().copy(
                alpha = 0.12f
            ),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp
        )
    }
}

/**
 * 멤버 선택 바텀시트 상단 헤더
 *
 * 검색 화면에서는 우측에 확인 버튼을 표시합니다.
 */
@Composable
fun GroupCreatePickerHeader(
    title: String,
    showConfirmButton: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        if (showConfirmButton) {
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
                cornerRadius = 8.dp
            )
        }
    }
}

/**
 * 멤버 프로필 이미지를 표시합니다.
 *
 * 프로필 이미지 URL이 존재하면 실제 사용자 이미지를 표시하고,
 * URL이 null 또는 빈 문자열이면 기본 프로필 아이콘을 표시합니다.
 */
@Composable
fun GroupCreateMemberProfile(
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
                id = R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * 확인 버튼이 필요하지 않은 단순 바텀시트 제목
 *
 * 파트장 선택 화면 등에서 사용합니다.
 */
@Composable
fun GroupCreatePickerTitle(
    title: String,
) {
    UText(
        text = title,
        style = UmcTypographyTokens.Title3Bold,
        color = grey800(),
        modifier = Modifier.padding(top = 4.dp)
    )
}

/**
 * 현재 선택되어 있는 스터디원 목록을 표시합니다.
 *
 * 선택된 멤버가 없으면 빈 화면을 표시합니다.
 */
@Composable
private fun CurrentMemberContent(
    members: ImmutableList<AdminStudyGroupCreateMemberUiModel>,
    onRemoveMember: (AdminStudyGroupCreateMemberUiModel) -> Unit,
) {
    if (members.isEmpty()) {
        GroupCreateEmptyContent(
            text = "아직 추가한 스터디원이 없어요"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = members,
            key = { member ->
                member.id
            }
        ) { member ->
            GroupCreateAddedMemberRow(
                item = member,
                onRemoveClick = {
                    onRemoveMember(member)
                }
            )
        }
    }
}

/**
 * 스터디원 검색 결과를 표시합니다.
 *
 * 검색 결과는 파트별로 그룹화하여 표시하며,
 * 이미 선택된 멤버는 다시 선택할 수 없습니다.
 *
 * 목록 하단에 도달하고 다음 페이지가 존재하면
 * 추가 검색 결과를 조회합니다.
 */
@Composable
private fun MemberSearchResultContent(
    state: GroupCreateMemberPickerState,
    onToggleMember: (AdminStudyGroupCreateMemberUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val groupedResults = state.searchResults
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        groupedResults.forEach { (partLabel, members) ->

            // 파트 구분 헤더
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

            items(
                items = members,
                key = { member ->
                    member.id
                }
            ) { member ->

                // 이미 최종 선택된 멤버인지 확인
                val isAlreadyMember =
                    state.selectedMembers.any { currentMember ->
                        currentMember.id == member.id
                    }

                // 현재 검색 화면에서 임시 선택한 멤버인지 확인
                val isPending =
                    state.pendingMembers.any { pendingMember ->
                        pendingMember.id == member.id
                    }

                GroupCreateMultiSearchRow(
                    item = member,
                    isChecked = isAlreadyMember || isPending,
                    enabled = !isAlreadyMember,
                    onToggleClick = {
                        onToggleMember(member)
                    }
                )
            }

            item(
                key = "part_spacing_$partLabel"
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }

        // 다음 페이지가 존재하면 추가 데이터 요청
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
 * 멤버 검색 결과의 개별 사용자 Row
 *
 * 프로필 이미지, 이름, 학교, 선택 체크박스를 표시합니다.
 *
 * 이미 기존 멤버인 경우에는 선택할 수 없습니다.
 */
@Composable
fun GroupCreateMultiSearchRow(
    item: AdminStudyGroupCreateMemberUiModel,
    isChecked: Boolean,
    enabled: Boolean = true,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onToggleClick
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사용자 프로필 이미지
        GroupCreateMemberProfile(
            profileImageUrl = item.profileImageUrl,
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = if (enabled) {
                    grey800()
                } else {
                    grey500()
                }
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = if (enabled) {
                    grey800()
                } else {
                    grey500()
                }
            )
        }

        // 멤버 선택 체크박스
        Icon(
            painter = painterResource(
                id = if (isChecked) {
                    R.drawable.ic_check_box_primary
                } else {
                    R.drawable.ic_check_box_empty
                }
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
    }
}