package com.umc.presentation.community.create.bottomsheet

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.umc.presentation.community.model.CommunityChallengerUiModel

/**
 * 스레드 생성 시 챌린저를 추가하는 BottomSheet
 *
 * - 챌린저 이름 검색
 * - 챌린저 선택/삭제
 * - 최대 선택 인원 제한
 * - 검색 결과 페이지네이션
 * - 프로필 이미지 표시
 *
 * 검색 및 선택 상태는 CommunityCreateMemberBottomSheetViewModel에서 관리합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreateMemberBottomSheet(
    preSelected: List<CommunityChallengerUiModel>,
    maxCount: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (List<CommunityChallengerUiModel>) -> Unit,
    onCsvUploadClick: () -> Unit = {},
    viewModel: CommunityCreateMemberBottomSheetViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    /**
     * 바텀시트가 열릴 때
     * 기존에 선택되어 있던 챌린저 목록을 ViewModel에 전달합니다.
     */
    LaunchedEffect(
        preSelected,
        maxCount,
    ) {
        viewModel.initialize(
            preSelected = preSelected,
            maxCount = maxCount,
        )
    }

    /**
     * ViewModel에서 발생하는 일회성 이벤트 처리
     */
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

    /**
     * 바텀시트를 닫을 때 현재 선택된 챌린저를
     * 스레드 생성 화면에 반영합니다.
     */
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
            CommunityCreateMemberHeader(
                title = if (state.isSearching) {
                    "챌린저를 검색하세요"
                } else {
                    "챌린저를 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.selectedMembers.isNotEmpty(),
                onCsvUploadClick = onCsvUploadClick,
                onConfirmClick = {
                    viewModel.clearSearchOnly()
                },
            )

            Spacer(
                modifier = Modifier.height(18.dp),
            )

            /**
             * 챌린저 이름 검색
             *
             * 실제 검색 API 호출 및 디바운스는 ViewModel에서 처리합니다.
             */
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
                    // 최초 검색 결과 로딩
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(
                                Alignment.Center
                            ),
                            color = indigo500(),
                        )
                    }

                    // 검색 API 실패
                    state.errorMessage != null &&
                            state.searchResults.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = state.errorMessage.orEmpty(),
                        )
                    }

                    // 검색 결과 없음
                    state.isSearching &&
                            state.searchResults.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = "검색 결과가 없어요",
                        )
                    }

                    // 챌린저 검색 결과
                    state.isSearching -> {
                        CommunityCreateMemberSearchContent(
                            members = state.searchResults,
                            selectedMembers = state.selectedMembers,
                            maxCount = state.maxMemberCount,
                            isLoadingMore = state.isLoadingMore,
                            hasNext = state.hasNext,
                            onToggleClick = viewModel::toggleMember,
                            onLoadMore = viewModel::loadMoreMembers,
                        )
                    }

                    // 선택된 챌린저가 없는 경우
                    state.selectedMembers.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = "아직 추가한 챌린저가 없어요",
                        )
                    }

                    // 현재 선택된 챌린저 목록
                    else -> {
                        CommunityCreateSelectedMemberContent(
                            selectedMembers = state.selectedMembers,
                            onRemoveClick = viewModel::toggleMember,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 바텀시트 상단 Header
 *
 * 검색 중에는 우측에 확인 버튼을 표시합니다.
 */
@Composable
private fun CommunityCreateMemberHeader(
    title: String,
    showConfirmButton: Boolean,
    isConfirmEnabled: Boolean,
    onCsvUploadClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f),
        )

        /*
         * CSV 업로드 기능
         *
         * 현재 사용하지 않아 비활성화 상태입니다.
         */
//        CommunityCsvUploadButton(
//            onClick = onCsvUploadClick,
//        )

        if (showConfirmButton) {
            Spacer(
                modifier = Modifier.width(8.dp),
            )

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
 * 챌린저 검색 결과 목록
 *
 * 파트별로 검색 결과를 그룹화해서 표시하며,
 * 리스트 하단 접근 시 다음 페이지를 요청합니다.
 */
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

    // 검색 결과를 파트별로 그룹화
    val groupedMembers = members
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    /**
     * 리스트 끝에서 3개 전까지 스크롤했을 경우
     * 다음 페이지가 존재하면 추가 데이터를 조회합니다.
     */
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index

            lastVisibleItemIndex != null &&
                    lastVisibleItemIndex >=
                    listState.layoutInfo.totalItemsCount - 3 &&
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
        groupedMembers.forEach { (partLabel, partMembers) ->
            // 파트 Header
            item(
                key = "part_header_$partLabel",
            ) {
                UText(
                    text = partLabel,
                    style = UmcTypographyTokens.BodyBold,
                    color = grey900(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 4.dp,
                            bottom = 8.dp,
                        ),
                )
            }

            // 해당 파트의 챌린저 목록
            items(
                items = partMembers,
                key = { member ->
                    member.memberId
                },
            ) { member ->
                val isChecked =
                    selectedMembers.any { selectedMember ->
                        selectedMember.memberId ==
                                member.memberId
                    }

                // 최대 인원에 도달해도 이미 선택된 멤버는 선택 해제 가능
                val isEnabled =
                    isChecked ||
                            selectedMembers.size < maxCount

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

            item(
                key = "part_spacing_$partLabel",
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp),
                )
            }
        }

        // 다음 페이지 로딩
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

/**
 * 검색된 챌린저 한 명을 표시하는 Row
 */
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
            .padding(vertical = 12.dp),
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
                text = buildString {
                    append(member.name)

                    if (member.nickname.isNotBlank()) {
                        append("/")
                        append(member.nickname)
                    }

                    if (member.generation > 0) {
                        append("(")
                        append(member.generation)
                        append("기)")
                    }
                },
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
                text = member.school,
                style = UmcTypographyTokens.Footnote,
                color = if (isEnabled) {
                    grey800()
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

/**
 * 현재 선택된 챌린저 목록
 */
@Composable
private fun CommunityCreateSelectedMemberContent(
    selectedMembers: List<CommunityChallengerUiModel>,
    onRemoveClick: (CommunityChallengerUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
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

/**
 * 선택된 챌린저 한 명을 표시하는 Row
 */
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
                text = buildString {
                    append(member.name)

                    if (member.nickname.isNotBlank()) {
                        append("/")
                        append(member.nickname)
                    }

                    if (member.generation > 0) {
                        append("(")
                        append(member.generation)
                        append("기)")
                    }
                },
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800(),
            )

            Spacer(
                modifier = Modifier.height(2.dp),
            )

            UText(
                text = member.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800(),
                maxLines = 1,
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

/**
 * 검색 결과 또는 선택된 챌린저가 없을 때 표시하는 Empty UI
 */
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

/**
 * 챌린저 프로필 이미지
 *
 * 프로필 이미지 URL이 존재하면 해당 이미지를 표시하고,
 * URL이 없는 경우 기본 프로필 이미지를 표시합니다.
 */
@Composable
private fun CommunityCreateMemberProfile(
    profileImage: String,
) {
    if (profileImage.isNotBlank()) {
        AsyncImage(
            model = profileImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_profile_default,
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )
    }
}