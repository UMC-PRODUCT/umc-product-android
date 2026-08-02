package com.umc.presentation.community.bottomsheet

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.umc.presentation.community.model.CommunityInvitableMemberUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMemberBottomSheet(
    threadId: String,
    onDismissRequest: () -> Unit,
    onInviteSuccess: () -> Unit,
    viewModel: CommunityMemberBottomSheetViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(threadId) {
        viewModel.initialize(
            threadId = threadId,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CommunityMemberBottomSheetEvent.InviteSuccess -> {
                    Toast.makeText(
                        context,
                        "${event.invitedMemberCount}명을 초대했어요.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    viewModel.resetAfterDismiss()
                    onInviteSuccess()
                    onDismissRequest()
                }

                is CommunityMemberBottomSheetEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetAfterDismiss()
            onDismissRequest()
        },
        sheetState = sheetState,
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
            CommunityMemberBottomSheetHeader(
                title = if (state.isSearching) {
                    "챌린저를 검색하세요"
                } else {
                    "챌린저를 추가하세요"
                },
                selectedCountText = state.selectedCountText,
                isConfirmEnabled = state.isConfirmEnabled,
                isInviting = state.isInviting,
                onConfirmClick = {
                    viewModel.inviteSelectedMembers()
                },
            )

            Spacer(
                modifier = Modifier.height(18.dp),
            )

            UTextField(
                value = state.query,
                onValueChange = viewModel::searchMembers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(
                    id = R.drawable.ic_search,
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
                modifier = Modifier.height(28.dp),
            )

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

                    state.errorMessage != null &&
                            state.displayedMembers.isEmpty() -> {
                        CommunityMemberEmptyContent(
                            text = state.errorMessage.orEmpty(),
                        )
                    }

                    state.isEmpty -> {
                        CommunityMemberEmptyContent(
                            text = if (state.isSearching) {
                                "검색 결과가 없어요"
                            } else {
                                "초대 가능한 챌린저가 없어요"
                            },
                        )
                    }

                    else -> {
                        CommunityMemberSearchContent(
                            members = state.displayedMembers,
                            selectedMembers = state.selectedMembers,
                            maxCount = state.maxMemberCount,
                            isLoadingMore = state.isLoadingMore,
                            hasNext = state.hasNext,
                            onToggleClick = { member ->
                                viewModel.toggleMember(
                                    member = member,
                                )
                            },
                            onLoadMore = {
                                viewModel.loadMoreMembers()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityMemberBottomSheetHeader(
    title: String,
    selectedCountText: String,
    isConfirmEnabled: Boolean,
    isInviting: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = title,
                style = UmcTypographyTokens.Title3Bold,
                color = grey800(),
            )

            Spacer(
                modifier = Modifier.height(4.dp),
            )

            UText(
                text = selectedCountText,
                style = UmcTypographyTokens.Footnote,
                color = indigo500(),
            )
        }

        UButton(
            text = if (isInviting) {
                "초대 중"
            } else {
                "확인"
            },
            onClick = onConfirmClick,
            enabled = isConfirmEnabled,
            modifier = Modifier
                .width(64.dp)
                .height(36.dp),
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

@Composable
private fun CommunityMemberSearchContent(
    members: List<CommunityInvitableMemberUiModel>,
    selectedMembers: List<CommunityInvitableMemberUiModel>,
    maxCount: Int,
    isLoadingMore: Boolean,
    hasNext: Boolean,
    onToggleClick: (CommunityInvitableMemberUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index

            lastVisibleItemIndex != null &&
                    lastVisibleItemIndex >= members.lastIndex - 2 &&
                    hasNext &&
                    !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        items(
            items = members,
            key = { member ->
                member.memberId
            },
        ) { member ->
            val isChecked = selectedMembers.any { selectedMember ->
                selectedMember.memberId == member.memberId
            }

            val isEnabled =
                isChecked || selectedMembers.size < maxCount

            CommunityMemberSearchRow(
                member = member,
                isChecked = isChecked,
                isEnabled = isEnabled,
                onToggleClick = {
                    if (isEnabled) {
                        onToggleClick(member)
                    }
                },
            )
        }

        if (isLoadingMore) {
            item(
                key = "loading_more",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = indigo500(),
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityMemberEmptyContent(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_people,
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(42.dp),
        )

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        UText(
            text = text,
            style = UmcTypographyTokens.Body,
            color = grey600(),
        )
    }
}

@Composable
private fun CommunityMemberSearchRow(
    member: CommunityInvitableMemberUiModel,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEnabled,
                onClick = onToggleClick,
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityMemberProfile()

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.name,
                style = UmcTypographyTokens.SubheadlineBold,
                color = if (isEnabled) {
                    grey800()
                } else {
                    grey400()
                },
            )

            Spacer(
                modifier = Modifier.height(2.dp),
            )

            UText(
                text = buildString {
                    append(member.partLabel)

                    if (member.generation.isNotBlank()) {
                        append(" · ")
                        append(member.generation)
                        append("기")
                    }
                },
                style = UmcTypographyTokens.Footnote,
                color = if (isEnabled) {
                    grey600()
                } else {
                    grey400()
                },
            )
        }

        Icon(
            painter = painterResource(
                id = if (isChecked) {
                    R.drawable.ic_check_box_primary
                } else {
                    R.drawable.ic_check_box_empty
                },
            ),
            contentDescription = if (isChecked) {
                "선택됨"
            } else {
                "선택되지 않음"
            },
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun CommunityMemberProfile() {
    Icon(
        painter = painterResource(
            id = R.drawable.ic_profile_default,
        ),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(32.dp),
    )
}