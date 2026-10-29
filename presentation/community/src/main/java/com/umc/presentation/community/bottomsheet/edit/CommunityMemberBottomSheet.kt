package com.umc.presentation.community.bottomsheet.edit

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList
import com.umc.component.base.CollectUiEvents

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
                title = if (state.isSearching) {
                    "초대할 챌린저를 검색하세요"
                } else {
                    "초대할 챌린저를 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.isConfirmEnabled,
                isUpdatingMembers = state.isUpdatingMembers,
                onConfirmClick = {
                    viewModel.updateMembers()
                },
            )

            Spacer(
                modifier = Modifier.height(18.dp),
            )

            /**
             * 챌린저 이름 검색 필드입니다.
             *
             * 실제 검색 API 호출과 300ms 디바운스 처리는
             * ViewModel에서 수행합니다.
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
                    // 최초 데이터 또는 검색 결과 로딩 중
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(
                                Alignment.Center,
                            ),
                            color = indigo500(),
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
                                "검색 결과가 없어요"
                            } else {
                                "현재 참여 중인 챌린저가 없어요"
                            },
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
 * 검색 중일 때만 확인 버튼을 표시합니다.
 */
@Composable
private fun CommunityMemberBottomSheetHeader(
    title: String,
    showConfirmButton: Boolean,
    isConfirmEnabled: Boolean,
    isUpdatingMembers: Boolean,
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

        if (showConfirmButton) {
            UButton(
                text = if (isUpdatingMembers) {
                    "처리 중"
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
            .padding(vertical = 12.dp),
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
                        color = indigo500(),
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
            .clickable(
                enabled = isEnabled,
                onClick = onToggleClick,
            )
            .padding(vertical = 12.dp),
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

/**
 * 목록에 표시할 멤버가 없을 때 사용하는 Empty UI입니다.
 */
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

/**
 * 멤버 프로필 이미지를 표시합니다.
 *
 * profileImage URL이 존재하면 실제 프로필 이미지를 표시하고,
 * URL이 없으면 기본 프로필 아이콘을 표시합니다.
 */
@Composable
private fun CommunityMemberProfile(
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
