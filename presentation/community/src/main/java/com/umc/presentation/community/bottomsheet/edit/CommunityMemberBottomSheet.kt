package com.umc.presentation.community.bottomsheet.edit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import com.umc.component.theme.grey950
import com.umc.presentation.community.component.chatting.CommunityChatProfileImage
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.main500
import com.umc.presentation.community.DEFAULT_COMMUNITY_MAX_MEMBER_COUNT
import com.umc.presentation.community.component.CommunityMemberSearchField
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * 커뮤니티 스레드의 현재 멤버를 관리하는 BottomSheet입니다.
 *
 * 주요 기능
 * - 현재 참여 중인 멤버 조회
 * - 챌린저 검색
 * - 멤버 선택/해제
 * - 새 멤버 초대
 * - 검색 결과 페이지네이션
 * - 프로필 이미지 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMemberBottomSheet(
    threadId: String,
    onDismissRequest: () -> Unit,
    onInviteSuccess: () -> Unit,
    viewModel: CommunityMemberBottomSheetViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val searchFocusRequester = remember { FocusRequester() }
    var searchFocused by remember { mutableStateOf(false) }
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f
    var toastData by remember(threadId) { mutableStateOf<UToastData?>(null) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * BottomSheet가 열릴 때 threadId를 기준으로
     * 현재 스레드 멤버 정보를 조회합니다.
     */
    LaunchedEffect(threadId) {
        viewModel.initialize(
            threadId = threadId,
        )
    }

    CommunityInviteSheet(
        height = sheetHeight,
        toast = {
            UToastHost(data = toastData, onDismiss = { toastData = null })
        },
        onDismissRequest = {
            viewModel.resetAfterDismiss()
            onDismissRequest()
        },
    ) {
        val focusManager = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current

        /**
         * ViewModel에서 발생한 일회성 이벤트를 처리합니다.
         *
         * 멤버 초대 성공 또는 에러 발생 시 Toast를 표시합니다.
         */
        CollectUiEvents(viewModel.uiEvent) { event ->
            when (event) {
                is CommunityMemberBottomSheetEvent.MemberUpdateSuccess -> {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    val message = "${event.addedMemberCount}명을 추가했어요."

                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT,
                    ).show()

                    onInviteSuccess()
                }

                is CommunityMemberBottomSheetEvent.ShowToast -> {
                    toastData = UToastData(event.message, UToastState.ERROR)
                }
            }
        }

        CommunityMemberSheetContent(
            state = state,
            isSearchFieldFocused = searchFocused,
            onQueryChange = viewModel::searchMembers,
            onConfirmClick = {
                if (state.isSearching || searchFocused) viewModel.updateMembers()
                else { searchFocusRequester.requestFocus(); keyboard?.show() }
            },
            onToggleMember = { member ->
                viewModel.toggleMember(
                    member = member,
                )
            },
            onLoadMore = {
                viewModel.loadMoreMembers()
            },
            modifier = Modifier.weight(1f),
            searchFieldModifier = Modifier.focusRequester(searchFocusRequester).onFocusChanged { searchFocused = it.isFocused },
        )
    }
}

/**
 * BottomSheet 안쪽 콘텐츠입니다.
 *
 * ViewModel·포커스 제어와 분리해 Preview에서 상태별로 그릴 수 있도록 합니다.
 */
@Composable
private fun CommunityMemberSheetContent(
    state: CommunityMemberBottomSheetState,
    isSearchFieldFocused: Boolean,
    onQueryChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onToggleMember: (CommunityChallengerUiModel) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    searchFieldModifier: Modifier = Modifier,
) {
    // 검색 필드에 포커스만 둬도 검색 화면 헤더(초대 버튼)로 바뀐다
    val isSearchMode = state.isSearching || isSearchFieldFocused

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        CommunityMemberBottomSheetHeader(
            title = if (isSearchMode) {
                "초대할 챌린저를 검색하세요"
            } else {
                "초대할 챌린저를 추가하세요"
            },
            isSearching = isSearchMode,
            isConfirmEnabled = if (isSearchMode) state.isConfirmEnabled else state.canManage && !state.isUpdatingMembers,
            isUpdatingMembers = state.isUpdatingMembers,
            onConfirmClick = onConfirmClick,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        /**
         * 챌린저 이름 검색 필드입니다.
         *
         * 실제 검색 API 호출과 300ms 디바운스 처리는
         * ViewModel에서 수행합니다.
         */
        CommunityMemberSearchField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = searchFieldModifier,
            placeholder = "이름을 입력해주세요",
            fieldHeight = 52.dp,
            showClearButton = true,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                // 최초 데이터 또는 검색 결과 로딩 중
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center,
                        ),
                        color = main500(),
                    )
                }

                // 멤버 조회 또는 검색 실패
                state.errorMessage != null &&
                        state.displayedMembers.isEmpty() -> {
                    CommunityMemberEmptyContent(
                        text = state.errorMessage.orEmpty(),
                    )
                }

                // 현재 멤버 또는 검색 결과가 없는 상태
                state.isEmpty -> {
                    CommunityMemberEmptyContent(
                        text = if (state.isSearching) {
                            "검색 결과가 없습니다."
                        } else {
                            "아직 초대한 챌린저가 없어요"
                        },
                        iconRes = if (state.isSearching) R.drawable.ic_search else R.drawable.ic_community_invite_empty,
                    )
                }

                // 검색 중일 때 검색 결과 표시
                state.isSearching -> {
                    CommunityMemberSearchContent(
                        members = state.displayedMembers,
                        selectedMembers = state.selectedMembers,
                        enabled = state.canManage && !state.isUpdatingMembers,
                        isLoadingMore = state.isLoadingMore,
                        hasNext = state.hasNext,
                        errorMessage = state.errorMessage,
                        onToggleClick = onToggleMember,
                        onLoadMore = onLoadMore,
                    )
                }

                // 검색 중이 아닐 때 현재 스레드 멤버 표시
                else -> {
                    CommunityCurrentMemberContent(
                        members = state.currentMembers,
                    )
                }
            }
        }
    }
}

/**
 * BottomSheet 상단 Header입니다.
 *
 * 기본 화면은 추가, 검색/선택 화면은 초대 버튼을 표시합니다.
 */
@Composable
internal fun CommunityMemberBottomSheetHeader(
    title: String,
    isSearching: Boolean,
    isConfirmEnabled: Boolean,
    isUpdatingMembers: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey950(),
            modifier = Modifier.weight(1f).heightIn(min = 32.dp),
        )
        UButton(
            text = if (isUpdatingMembers) "처리 중" else if (isSearching) "초대" else "추가",
            onClick = onConfirmClick,
            enabled = isConfirmEnabled,
            modifier = Modifier.widthIn(min = 50.dp).heightIn(min = 32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            backgroundColor = if (isConfirmEnabled) main500() else grey100(),
            textColor = if (isConfirmEnabled) grey000() else grey400(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 8.dp,
        )
    }
}

/**
 * 현재 스레드에 참여 중인 멤버 목록을 표시합니다.
 */
@Composable
internal fun CommunityCurrentMemberContent(
    members: ImmutableList<CommunityChallengerUiModel>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = members,
            key = { member ->
                member.memberId
            },
        ) { member ->
            CommunityCurrentMemberRow(
                member = member,
            )
        }
    }
}

/**
 * 현재 참여 중인 멤버 한 명을 표시하는 Row입니다.
 */
@Composable
private fun CommunityCurrentMemberRow(
    member: CommunityChallengerUiModel,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityMemberProfile(
            profileImage = member.profileImage,
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = buildMemberTitle(
                    member = member,
                ),
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800(),
                maxLines = 1,
            )

            Spacer(
                modifier = Modifier.height(2.dp),
            )

            UText(
                text = member.school,
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
                maxLines = 1,
            )
        }

    }
}

/**
 * 검색된 챌린저 목록을 표시합니다.
 *
 * 검색 결과를 파트별로 그룹화하고,
 * 리스트 하단 접근 시 다음 페이지를 요청합니다.
 */
@Composable
internal fun CommunityMemberSearchContent(
    members: ImmutableList<CommunityChallengerUiModel>,
    selectedMembers: ImmutableList<CommunityChallengerUiModel>,
    enabled: Boolean,
    isLoadingMore: Boolean,
    hasNext: Boolean,
    errorMessage: String? = null,
    onToggleClick: (CommunityChallengerUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    // 파트별로 챌린저 검색 결과를 그룹화
    val groupedMembers = members
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    // derivedStateOf 는 처음 한 번만 만들어지므로, 파라미터를 직접 읽으면 처음 값에 묶여 다음 페이지를 영영 부르지 못한다
    val currentHasNext by rememberUpdatedState(hasNext)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)
    // 추가 페이지 요청이 실패하면 hasNext 가 남아 같은 요청을 쉬지 않고 반복하므로, 오류가 있는 동안은 자동으로 부르지 않는다(검색어를 바꾸면 오류가 지워진다)
    val currentHasError by rememberUpdatedState(errorMessage != null)

    /**
     * 리스트 마지막 영역에 가까워졌을 때
     * 다음 페이지가 존재하면 추가 검색을 요청합니다.
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
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        groupedMembers.forEach { (partLabel, partMembers) ->
            item(
                key = "part_header_$partLabel",
            ) {
                UText(
                    text = partLabel,
                    style = UmcTypographyTokens.Callout,
                    color = grey950(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 0.dp,
                            bottom = 0.dp,
                        ),
                )
            }

            items(
                items = partMembers,
                key = { member ->
                    member.memberId
                },
            ) { member ->
                val isChecked =
                    selectedMembers.any { selectedMember ->
                        selectedMember.memberId == member.memberId
                    }

                CommunityMemberSearchRow(
                    member = member,
                    isChecked = isChecked,
                    isEnabled = enabled,
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

        // 다음 페이지 조회 중 표시
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
                        color = main500(),
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

/**
 * 검색 결과의 챌린저 한 명을 표시하는 Row입니다.
 */
@Composable
private fun CommunityMemberSearchRow(
    member: CommunityChallengerUiModel,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isChecked, enabled = isEnabled, role = Role.Checkbox,
                onValueChange = { onToggleClick() })
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityMemberProfile(
            profileImage = member.profileImage,
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = buildMemberTitle(
                    member = member,
                ),
                style = UmcTypographyTokens.SubheadlineBold,
                color = if (isEnabled) {
                    grey800()
                } else {
                    grey400()
                },
                maxLines = 1,
            )

            Spacer(
                modifier = Modifier.height(2.dp),
            )

            UText(
                text = member.school,
                style = UmcTypographyTokens.Caption1,
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

/**
 * 목록에 표시할 멤버가 없을 때 사용하는 Empty UI입니다.
 */
@Composable
internal fun CommunityMemberEmptyContent(
    text: String,
    @DrawableRes iconRes: Int = R.drawable.ic_community_invite_empty,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(
                id = iconRes,
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(48.dp),
        )

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        UText(
            text = text,
            style = UmcTypographyTokens.Callout,
            color = grey600(),
        )
    }
}

/**
 * 멤버 프로필 이미지를 표시합니다.
 *
 * profileImage URL이 존재하면 실제 프로필 이미지를 표시하고,
 * URL이 없으면 기본 프로필 아이콘을 표시합니다.
 */
@Composable
private fun CommunityMemberProfile(profileImage: String) {
    CommunityChatProfileImage(profileImage, useThreadStyle = true)
}

/**
 * 챌린저 이름/닉네임과 기수 정보를 화면 표시용 문자열로 변환합니다.
 */
private fun buildMemberTitle(
    member: CommunityChallengerUiModel,
): String {
    return buildString {
        append(member.displayName)

        if (member.generation > 0) {
            append("(")
            append(member.generation)
            append("기)")
        }
    }
}

/**
 * Preview용 시트 틀입니다.
 *
 * 실제 시트는 Dialog 위에 그려져 Preview에 나오지 않으므로,
 * 시트 높이·배경·핸들 영역(48dp)과 하단 토스트 위치만 흉내 냅니다.
 */
@Composable
internal fun CommunityMemberSheetPreviewFrame(
    toastData: UToastData? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .background(grey000()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            content = content,
        )

        UToastHost(
            data = toastData,
            onDismiss = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )
    }
}

private val previewCurrentMembers = persistentListOf(
    CommunityChallengerUiModel(
        memberId = 1L,
        name = "김챌린저",
        nickname = "챌린",
        school = "UMC대학교",
        generation = 9L,
        partLabel = "Android",
    ),
    CommunityChallengerUiModel(
        memberId = 2L,
        name = "이유엠",
        nickname = "",
        school = "한국대학교",
        generation = 9L,
        partLabel = "iOS",
    ),
    CommunityChallengerUiModel(
        memberId = 3L,
        name = "박프로덕트",
        nickname = "프덕",
        school = "서울대학교",
        generation = 8L,
        partLabel = "Web",
    ),
)

// 검색 결과에 이미 참여 중인 멤버(1번)를 섞어, displayedMembers에서 걸러지는지 함께 본다
private val previewSearchResults = persistentListOf(
    previewCurrentMembers[0],
    CommunityChallengerUiModel(
        memberId = 11L,
        name = "김안드",
        nickname = "안드",
        school = "UMC대학교",
        generation = 9L,
        partLabel = "Android",
    ),
    CommunityChallengerUiModel(
        memberId = 12L,
        name = "김코틀린",
        nickname = "",
        school = "한국대학교",
        generation = 9L,
        partLabel = "Android",
    ),
    CommunityChallengerUiModel(
        memberId = 13L,
        name = "김스위프트",
        nickname = "스윕",
        school = "부산대학교",
        generation = 8L,
        partLabel = "iOS",
    ),
)

// 기존 멤버 + 새로 고른 1명 → addedMembers가 생겨 초대 버튼이 활성화된다
private val previewSelectedMembers =
    (previewCurrentMembers + previewSearchResults[1]).toImmutableList()

@Composable
private fun CommunityMemberSheetPreviewContent(
    state: CommunityMemberBottomSheetState,
    modifier: Modifier = Modifier,
    isSearchFieldFocused: Boolean = false,
) {
    CommunityMemberSheetContent(
        state = state,
        isSearchFieldFocused = isSearchFieldFocused,
        onQueryChange = {},
        onConfirmClick = {},
        onToggleMember = {},
        onLoadMore = {},
        modifier = modifier,
    )
}

/**
 * 현재 참여 중인 멤버 목록 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetCurrentMembersPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                currentMembers = previewCurrentMembers,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 참여 멤버가 없는 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetEmptyPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 멤버 조회 로딩 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetLoadingPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                isLoading = true,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 검색 필드에 포커스만 두고 아직 고르지 않은 상태 Preview (초대 버튼 비활성)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetSearchFocusedPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                currentMembers = previewCurrentMembers,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 검색 결과에서 새 멤버를 고른 상태 Preview (초대 버튼 활성)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetSearchPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                query = "김",
                isSearching = true,
                currentMembers = previewCurrentMembers,
                searchResults = previewSearchResults,
                selectedMembers = previewSelectedMembers,
                hasNext = true,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 검색 결과가 없는 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetSearchNoResultPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                query = "없는이름",
                isSearching = true,
                currentMembers = previewCurrentMembers,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 초대를 서버에 반영 중이면서 다음 페이지를 불러오는 상태 Preview
 * (헤더 "처리 중", 검색 결과 비활성)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetUpdatingPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                query = "김",
                isSearching = true,
                isLoadingMore = true,
                isUpdatingMembers = true,
                currentMembers = previewCurrentMembers,
                searchResults = previewSearchResults,
                selectedMembers = previewSelectedMembers,
                hasNext = true,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 멤버 조회·검색 실패 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetErrorPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                query = "김",
                isSearching = true,
                errorMessage = "챌린저를 불러오지 못했어요.",
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 최대 인원 초과 UToast가 표시된 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityMemberSheetMaxCountToastPreview() {
    CommunityMemberSheetPreviewFrame(
        toastData = UToastData(
            message = "최대 ${DEFAULT_COMMUNITY_MAX_MEMBER_COUNT}명까지 초대할 수 있습니다.",
            state = UToastState.ERROR,
        ),
    ) {
        CommunityMemberSheetPreviewContent(
            state = CommunityMemberBottomSheetState(
                canManage = true,
                query = "김",
                isSearching = true,
                currentMembers = previewCurrentMembers,
                searchResults = previewSearchResults,
                selectedMembers = previewSelectedMembers,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}
