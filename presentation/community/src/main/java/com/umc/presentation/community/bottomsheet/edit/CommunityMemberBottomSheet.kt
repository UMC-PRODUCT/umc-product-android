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
import com.umc.component.theme.white
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.main500
import com.umc.presentation.community.component.CommunityMemberSearchField
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            CommunityMemberBottomSheetHeader(
                title = if (state.isSearching || searchFocused) {
                    "초대할 챌린저를 검색하세요"
                } else {
                    "초대할 챌린저를 추가하세요"
                },
                isSearching = state.isSearching || searchFocused,
                isConfirmEnabled = if (state.isSearching || searchFocused) state.isConfirmEnabled else state.canManage && !state.isUpdatingMembers,
                isUpdatingMembers = state.isUpdatingMembers,
                onConfirmClick = {
                    if (state.isSearching || searchFocused) viewModel.updateMembers()
                    else { searchFocusRequester.requestFocus(); keyboard?.show() }
                },
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
                onValueChange = viewModel::searchMembers,
                modifier = Modifier.focusRequester(searchFocusRequester).onFocusChanged { searchFocused = it.isFocused },
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
}

/**
 * BottomSheet 상단 Header입니다.
 *
 * 기본 화면은 추가, 검색/선택 화면은 초대 버튼을 표시합니다.
 */
@Composable
private fun CommunityMemberBottomSheetHeader(
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
            textColor = if (isConfirmEnabled) white() else grey400(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 8.dp,
        )
    }
}

/**
 * 현재 스레드에 참여 중인 멤버 목록을 표시합니다.
 */
@Composable
private fun CommunityCurrentMemberContent(
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
private fun CommunityMemberSearchContent(
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
private fun CommunityMemberEmptyContent(
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
