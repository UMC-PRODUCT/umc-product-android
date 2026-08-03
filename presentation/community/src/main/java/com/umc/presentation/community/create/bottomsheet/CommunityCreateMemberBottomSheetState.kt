package com.umc.presentation.community.create.bottomsheet

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
import com.umc.presentation.community.model.CommunityChallengerUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreateMemberBottomSheet(
    preSelected: List<CommunityChallengerUiModel>,
    maxCount: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (List<CommunityChallengerUiModel>) -> Unit,
    viewModel: CommunityCreateMemberBottomSheetViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(
        preSelected,
        maxCount,
    ) {
        viewModel.initialize(
            preSelected = preSelected,
            maxCount = maxCount,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CommunityCreateMemberBottomSheetEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    fun dismissWithApply() {
        onConfirm(
            state.selectedMembers
                .distinctBy { member ->
                    member.memberId
                }
                .take(state.maxMemberCount)
        )

        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
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
            CommunityCreateMemberBottomSheetHeader(
                title = if (state.isSearching) {
                    "챌린저를 검색하세요"
                } else {
                    "챌린저를 추가하세요"
                },
                selectedCountText = state.selectedCountText,
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.isConfirmEnabled,
                onConfirmClick = {
                    viewModel.clearSearchOnly()
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
                            state.searchResults.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = state.errorMessage.orEmpty(),
                        )
                    }

                    state.isSearching && state.isEmpty -> {
                        CommunityCreateMemberEmptyContent(
                            text = "검색 결과가 없어요",
                        )
                    }

                    state.isSearching -> {
                        CommunityCreateMemberSearchContent(
                            members = state.searchResults,
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

                    state.selectedMembers.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = "아직 추가한 챌린저가 없어요",
                        )
                    }

                    else -> {
                        CommunityCreateSelectedMemberContent(
                            selectedMembers = state.selectedMembers,
                            maxCount = state.maxMemberCount,
                            onRemoveClick = { member ->
                                viewModel.toggleMember(
                                    member = member,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityCreateMemberBottomSheetHeader(
    title: String,
    selectedCountText: String,
    showConfirmButton: Boolean,
    isConfirmEnabled: Boolean,
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
                cornerRadius = 8.dp,
            )
        }
    }
}

@Composable
private fun CommunityCreateMemberSearchContent(
    members: List<CommunityChallengerUiModel>,
    selectedMembers: List<CommunityChallengerUiModel>,
    maxCount: Int,
    isLoadingMore: Boolean,
    hasNext: Boolean,
    onToggleClick: (CommunityChallengerUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index

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

            CommunityCreateMemberSearchRow(
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
private fun CommunityCreateSelectedMemberContent(
    selectedMembers: List<CommunityChallengerUiModel>,
    maxCount: Int,
    onRemoveClick: (CommunityChallengerUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item(
            key = "selected_count",
        ) {
            UText(
                text = "추가한 챌린저 ${selectedMembers.size} / $maxCount",
                style = UmcTypographyTokens.BodyBold,
                color = grey900(),
                modifier = Modifier.padding(
                    bottom = 8.dp,
                ),
            )
        }

        items(
            items = selectedMembers,
            key = { member ->
                member.memberId
            },
        ) { member ->
            CommunityCreateSelectedMemberRow(
                member = member,
                onRemoveClick = {
                    onRemoveClick(member)
                },
            )
        }
    }
}

@Composable
private fun CommunityCreateMemberSearchRow(
    member: CommunityChallengerUiModel,
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
        CommunityCreateMemberProfile(
            profileImage = member.profileImage,
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
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

                    if (member.generation > 0) {
                        append(" · ")
                        append(member.generation)
                        append("기")
                    }

                    if (member.school.isNotBlank()) {
                        append(" · ")
                        append(member.school)
                    }
                },
                style = UmcTypographyTokens.Footnote,
                color = if (isEnabled) {
                    grey600()
                } else {
                    grey400()
                },
                maxLines = 1,
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
private fun CommunityCreateSelectedMemberRow(
    member: CommunityChallengerUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityCreateMemberProfile(
            profileImage = member.profileImage,
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800(),
            )

            Spacer(
                modifier = Modifier.height(2.dp),
            )

            UText(
                text = member.partLabel,
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
            )
        }

        UButton(
            text = "삭제",
            onClick = onRemoveClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor = red500().copy(
                alpha = 0.12f,
            ),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp,
        )
    }
}

@Composable
private fun CommunityCreateMemberEmptyContent(
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
private fun CommunityCreateMemberProfile(
    profileImage: String,
) {

    Icon(
        painter = painterResource(
            id = R.drawable.ic_profile_default,
        ),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(32.dp),
    )
}