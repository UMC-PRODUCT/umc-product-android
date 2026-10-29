package com.umc.presentation.community.bottomsheet.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.umc.component.base.CollectUiEvents

/**
 * 스레드 생성 시 챌린저를 추가하는 BottomSheet
 *
 * - 챌린저 이름 검색
 * - 챌린저 선택/삭제
 * - 초대 시 최대 인원 검사 (통과해야 스레드 생성 화면에 반영)
 * - 검색 결과 페이지네이션
 * - 프로필 이미지 표시
 *
 * 검색 및 선택 상태는 CommunityCreateMemberBottomSheetViewModel에서 관리합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreateMemberBottomSheet(
    preSelected: ImmutableList<CommunityChallengerUiModel>,
    maxCount: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (List<CommunityChallengerUiModel>) -> Unit,
    onCsvUploadClick: () -> Unit = {},
    viewModel: CommunityCreateMemberBottomSheetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    // 검색 TextField 포커스 여부에 따라 헤더 문구를 바꾸기 위해 관찰
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isSearchFieldFocused by searchInteractionSource.collectIsFocusedAsState()

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
    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is CommunityCreateMemberBottomSheetEvent.ShowToast -> {
                toastData = UToastData(
                    message = event.message,
                    state = UToastState.ERROR,
                )
            }

            // 초대 버튼에서 인원 검사를 통과했을 때만 스레드 생성 화면에 반영
            is CommunityCreateMemberBottomSheetEvent.InviteMembers -> {
                onConfirm(event.members)
                viewModel.resetAfterDismiss()
                onDismissRequest()
            }
        }
    }

    /**
     * 바텀시트를 그냥 닫으면 선택 변경 사항은 버리고
     * 스레드 생성 화면의 기존 선택을 유지합니다.
     */
    fun dismissWithoutApply() {
        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithoutApply,
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
        CommunityCreateMemberSheetContent(
            state = state,
            onQueryChange = viewModel::searchMembers,
            onToggleMember = viewModel::toggleMember,
            onLoadMore = viewModel::loadMoreMembers,
            onInviteClick = viewModel::onInviteClick,
            onCsvUploadClick = onCsvUploadClick,
            isSearchFieldFocused = isSearchFieldFocused,
            searchInteractionSource = searchInteractionSource,
            toastData = toastData,
            onToastDismiss = { toastData = null },
        )
    }
}

/**
 * 챌린저 추가 BottomSheet 내부 콘텐츠
 *
 * Preview에서도 사용할 수 있게
 * ViewModel 없이 상태와 콜백만 받아 그리도록 함
 */
@Composable
private fun CommunityCreateMemberSheetContent(
    state: CommunityCreateMemberBottomSheetState,
    onQueryChange: (String) -> Unit,
    onToggleMember: (CommunityChallengerUiModel) -> Unit,
    onLoadMore: () -> Unit,
    onInviteClick: () -> Unit,
    onCsvUploadClick: () -> Unit,
    isSearchFieldFocused: Boolean = false,
    searchInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    toastData: UToastData? = null,
    onToastDismiss: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(620.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            CommunityCreateMemberHeader(
                // 검색 TextField에 포커스가 있는 동안만 검색 문구 표시
                title = if (isSearchFieldFocused) {
                    AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_TITLE
                } else {
                    AppStrings.COMMUNITY_CREATE_MEMBER_ADD_TITLE
                },
                isInviteEnabled = state.isConfirmEnabled,
                onCsvUploadClick = onCsvUploadClick,
                onInviteClick = onInviteClick,
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
                onValueChange = onQueryChange,
                placeholder = AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_PLACEHOLDER,
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
                interactionSource = searchInteractionSource,
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
                            text = AppStrings.COMMUNITY_CREATE_MEMBER_NO_RESULT,
                        )
                    }

                    // 챌린저 검색 결과
                    state.isSearching -> {
                        CommunityCreateMemberSearchContent(
                            members = state.searchResults,
                            selectedMembers = state.selectedMembers,
                            isLoadingMore = state.isLoadingMore,
                            hasNext = state.hasNext,
                            errorMessage = state.errorMessage,
                            onToggleClick = onToggleMember,
                            onLoadMore = onLoadMore,
                        )
                    }

                    // 선택된 챌린저가 없는 경우
                    state.selectedMembers.isEmpty() -> {
                        CommunityCreateMemberEmptyContent(
                            text = AppStrings.COMMUNITY_CREATE_MEMBER_EMPTY,
                        )
                    }

                    // 현재 선택된 챌린저 목록
                    else -> {
                        CommunityCreateSelectedMemberContent(
                            selectedMembers = state.selectedMembers,
                            onRemoveClick = onToggleMember,
                        )
                    }
                }
            }
        }

        // 최대 인원 초과·검색 실패 UToast: 시트 하단 16dp 지점 (Figma SnackBar: grey950 배경 / grey050 글자)
        UToastHost(
            data = toastData,
            onDismiss = onToastDismiss,
            backgroundColor = grey950(),
            textColor = grey50(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }
}

/**
 * 바텀시트 상단 Header
 *
 * 우측 초대 버튼은 항상 표시하며, 선택된 챌린저가 있을 때만 활성화합니다.
 */
@Composable
private fun CommunityCreateMemberHeader(
    title: String,
    isInviteEnabled: Boolean,
    onCsvUploadClick: () -> Unit,
    onInviteClick: () -> Unit,
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

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        UButton(
            text = AppStrings.COMMUNITY_CREATE_MEMBER_INVITE,
            onClick = onInviteClick,
            enabled = isInviteEnabled,
            modifier = Modifier
                .width(52.dp)
                .height(32.dp),
            backgroundColor = if (isInviteEnabled) {
                indigo500()
            } else {
                grey100()
            },
            textColor = if (isInviteEnabled) {
                grey000()
            } else {
                grey400()
            },
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 8.dp,
        )
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
    members: ImmutableList<CommunityChallengerUiModel>,
    selectedMembers: ImmutableList<CommunityChallengerUiModel>,
    isLoadingMore: Boolean,
    hasNext: Boolean,
    errorMessage: String? = null,
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

    // derivedStateOf가 처음 값에 고정되지 않도록 인자를 State로 감싸 최신 값을 읽음
    val currentHasNext by rememberUpdatedState(hasNext)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)
    // 추가 페이지 요청이 실패하면 hasNext 가 남아 같은 요청을 쉬지 않고 반복하므로, 오류가 있는 동안은 자동으로 부르지 않는다(검색어를 바꾸면 오류가 지워진다)
    val currentHasError by rememberUpdatedState(errorMessage != null)

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
                    currentHasNext &&
                    !currentIsLoadingMore &&
                    !currentHasError
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberSheetScrollGuard()),
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

                // 최대 인원은 초대 버튼에서 검사하므로 선택은 막지 않음
                CommunityCreateMemberSearchRow(
                    member = member,
                    isChecked = isChecked,
                    isEnabled = true,
                    onToggleClick = {
                        onToggleClick(member)
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
                        append(
                            AppStrings.COMMUNITY_CREATE_MEMBER_GENERATION_FORMAT.format(member.generation)
                        )
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
                AppStrings.COMMUNITY_CREATE_MEMBER_CD_CHECKED
            } else {
                AppStrings.COMMUNITY_CREATE_MEMBER_CD_UNCHECKED
            },
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * 바텀시트 안 목록의 스크롤이 시트를 움직이거나 닫지 않도록 막는 NestedScrollConnection
 *
 * 목록이 쓰고 남은 이동량과 fling 속도를 모두 여기서 소비해 ModalBottomSheet로 넘기지 않습니다.
 * 목록 영역에서는 스와이프로 시트가 닫히지 않고, 시트는 드래그 핸들·헤더를 끌거나
 * 바깥 영역 탭, 뒤로가기로 닫습니다.
 */
@Composable
private fun rememberSheetScrollGuard(): NestedScrollConnection {
    return remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = available

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity = available
        }
    }
}

/**
 * 현재 선택된 챌린저 목록
 */
@Composable
private fun CommunityCreateSelectedMemberContent(
    selectedMembers: ImmutableList<CommunityChallengerUiModel>,
    onRemoveClick: (CommunityChallengerUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberSheetScrollGuard()),
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
                        append(
                            AppStrings.COMMUNITY_CREATE_MEMBER_GENERATION_FORMAT.format(member.generation)
                        )
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
            text = AppStrings.COMMUNITY_CREATE_MEMBER_DELETE,
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

/**
 * Preview용 챌린저 샘플 데이터
 */
private val previewChallengers = persistentListOf(
    CommunityChallengerUiModel(
        memberId = 1L,
        name = "김챌린저",
        nickname = "챌린",
        school = "UMC대학교",
        generation = 9L,
        partLabel = "Android",
        profileImage = "",
    ),
    CommunityChallengerUiModel(
        memberId = 2L,
        name = "이유엠",
        nickname = "유엠",
        school = "UMC대학교",
        generation = 9L,
        partLabel = "iOS",
        profileImage = "",
    ),
    CommunityChallengerUiModel(
        memberId = 3L,
        name = "박씨",
        nickname = "씨씨",
        school = "UMC대학교",
        generation = 8L,
        partLabel = "Server",
        profileImage = "",
    ),
)

/**
 * 추가한 챌린저가 없는 최초 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetEmptyPreview() {
    CommunityCreateMemberSheetContent(
        state = CommunityCreateMemberBottomSheetState(),
        onQueryChange = {},
        onToggleMember = {},
        onLoadMore = {},
        onInviteClick = {},
        onCsvUploadClick = {},
    )
}

/**
 * 챌린저를 추가한 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetSelectedPreview() {
    CommunityCreateMemberSheetContent(
        state = CommunityCreateMemberBottomSheetState(
            selectedMembers = previewChallengers.take(2).toImmutableList(),
        ),
        onQueryChange = {},
        onToggleMember = {},
        onLoadMore = {},
        onInviteClick = {},
        onCsvUploadClick = {},
    )
}

/**
 * 챌린저 검색 결과 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetSearchPreview() {
    CommunityCreateMemberSheetContent(
        state = CommunityCreateMemberBottomSheetState(
            query = "김",
            isSearching = true,
            searchResults = previewChallengers,
            selectedMembers = previewChallengers.take(1).toImmutableList(),
        ),
        onQueryChange = {},
        onToggleMember = {},
        onLoadMore = {},
        onInviteClick = {},
        onCsvUploadClick = {},
    )
}

/**
 * 최대 인원 초과 UToast가 표시된 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetMaxCountToastPreview() {
    CommunityCreateMemberSheetContent(
        state = CommunityCreateMemberBottomSheetState(
            query = "김",
            isSearching = true,
            searchResults = previewChallengers,
            selectedMembers = previewChallengers.take(1).toImmutableList(),
        ),
        onQueryChange = {},
        onToggleMember = {},
        onLoadMore = {},
        onInviteClick = {},
        onCsvUploadClick = {},
        toastData = UToastData(
            message = AppStrings.COMMUNITY_CREATE_MEMBER_MAX_COUNT_FORMAT.format(99),
            state = UToastState.ERROR,
        ),
    )
}
