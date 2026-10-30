package com.umc.presentation.community.bottomsheet.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.main500
import com.umc.presentation.community.bottomsheet.edit.CommunityCurrentMemberContent
import com.umc.presentation.community.bottomsheet.edit.CommunityInviteSheet
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberBottomSheetHeader
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberEmptyContent
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberSearchContent
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberSheetPreviewFrame
import com.umc.presentation.community.component.CommunityMemberSearchField
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * 스레드 생성 시 챌린저를 추가하는 BottomSheet
 *
 * 스레드 멤버 관리 BottomSheet(CommunityMemberBottomSheet)와 같은 시트·헤더·목록 UI를 씁니다.
 *
 * - 챌린저 이름 검색
 * - 검색 결과 체크박스로 챌린저 선택/해제 (선택 목록에는 삭제 버튼을 두지 않음)
 * - 최대 인원까지만 선택 가능 (초과 선택 시 Toast, 초대 버튼을 눌러야 스레드 생성 화면에 반영)
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
    val searchFocusRequester = remember { FocusRequester() }
    var searchFocused by remember { mutableStateOf(false) }
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f
    var toastData by remember { mutableStateOf<UToastData?>(null) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
     * 바텀시트를 그냥 닫으면 선택 변경 사항은 버리고
     * 스레드 생성 화면의 기존 선택을 유지합니다.
     */
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
         * ViewModel에서 발생하는 일회성 이벤트 처리
         */
        CollectUiEvents(viewModel.uiEvent) { event ->
            when (event) {
                is CommunityCreateMemberBottomSheetEvent.ShowToast -> {
                    toastData = UToastData(event.message, UToastState.ERROR)
                }

                // 초대 버튼에서 인원 검사를 통과했을 때만 스레드 생성 화면에 반영
                is CommunityCreateMemberBottomSheetEvent.InviteMembers -> {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    onConfirm(event.members)
                    viewModel.resetAfterDismiss()
                    onDismissRequest()
                }
            }
        }

        CommunityCreateMemberSheetContent(
            state = state,
            isSearchFieldFocused = searchFocused,
            onQueryChange = viewModel::searchMembers,
            onConfirmClick = {
                if (state.isSearching || searchFocused) viewModel.onInviteClick()
                else { searchFocusRequester.requestFocus(); keyboard?.show() }
            },
            onToggleMember = viewModel::toggleMember,
            onLoadMore = viewModel::loadMoreMembers,
            modifier = Modifier.weight(1f),
            searchFieldModifier = Modifier.focusRequester(searchFocusRequester).onFocusChanged { searchFocused = it.isFocused },
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
        // 평소엔 "추가"(검색 필드로 포커스 이동), 검색 중엔 "초대"(선택한 챌린저가 있을 때만 활성)
        CommunityMemberBottomSheetHeader(
            title = if (isSearchMode) {
                AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_TITLE
            } else {
                AppStrings.COMMUNITY_CREATE_MEMBER_ADD_TITLE
            },
            isSearching = isSearchMode,
            isConfirmEnabled = if (isSearchMode) state.isConfirmEnabled else true,
            isUpdatingMembers = false,
            onConfirmClick = onConfirmClick,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        /**
         * 챌린저 이름 검색
         *
         * 실제 검색 API 호출 및 디바운스는 ViewModel에서 처리합니다.
         */
        CommunityMemberSearchField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = searchFieldModifier,
            placeholder = AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_PLACEHOLDER,
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
                // 최초 검색 결과 로딩
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center,
                        ),
                        color = main500(),
                    )
                }

                // 검색 API 실패
                state.errorMessage != null &&
                        state.searchResults.isEmpty() -> {
                    CommunityMemberEmptyContent(
                        text = state.errorMessage.orEmpty(),
                    )
                }

                // 검색 결과 없음
                state.isEmpty -> {
                    CommunityMemberEmptyContent(
                        text = AppStrings.COMMUNITY_CREATE_MEMBER_NO_RESULT,
                        iconRes = R.drawable.ic_search,
                    )
                }

                // 챌린저 검색 결과 (최대 인원 초과 선택은 ViewModel이 막고 Toast를 띄움)
                state.isSearching -> {
                    CommunityMemberSearchContent(
                        members = state.searchResults,
                        selectedMembers = state.selectedMembers,
                        enabled = true,
                        isLoadingMore = state.isLoadingMore,
                        hasNext = state.hasNext,
                        errorMessage = state.errorMessage,
                        onToggleClick = onToggleMember,
                        onLoadMore = onLoadMore,
                    )
                }

                // 선택된 챌린저가 없는 경우
                state.selectedMembers.isEmpty() -> {
                    CommunityMemberEmptyContent(
                        text = AppStrings.COMMUNITY_CREATE_MEMBER_EMPTY,
                    )
                }

                // 현재 선택된 챌린저 목록
                else -> {
                    CommunityCurrentMemberContent(
                        members = state.selectedMembers,
                    )
                }
            }
        }
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

@Composable
private fun CommunityCreateMemberSheetPreviewContent(
    state: CommunityCreateMemberBottomSheetState,
    modifier: Modifier = Modifier,
    isSearchFieldFocused: Boolean = false,
) {
    CommunityCreateMemberSheetContent(
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
 * 추가한 챌린저가 없는 최초 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetEmptyPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 챌린저를 추가한 상태 Preview (삭제 버튼 없이 목록만 표시)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetSelectedPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                selectedMembers = previewChallengers.take(2).toImmutableList(),
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 검색 필드에 포커스만 둔 상태 Preview (선택한 챌린저가 있어 초대 버튼 활성)
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetSearchFocusedPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                selectedMembers = previewChallengers.take(2).toImmutableList(),
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 챌린저 검색 결과 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetSearchPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                query = "김",
                isSearching = true,
                searchResults = previewChallengers,
                selectedMembers = previewChallengers.take(1).toImmutableList(),
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
private fun CommunityCreateMemberSheetSearchNoResultPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                query = "없는이름",
                isSearching = true,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 검색 결과 로딩 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetLoadingPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                query = "김",
                isSearching = true,
                isLoading = true,
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}

/**
 * 검색 실패 상태 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCreateMemberSheetErrorPreview() {
    CommunityMemberSheetPreviewFrame {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                query = "김",
                isSearching = true,
                errorMessage = AppStrings.COMMUNITY_CREATE_MEMBER_SEARCH_FAILED,
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
private fun CommunityCreateMemberSheetMaxCountToastPreview() {
    CommunityMemberSheetPreviewFrame(
        toastData = UToastData(
            message = AppStrings.COMMUNITY_CREATE_MEMBER_MAX_COUNT_FORMAT.format(99),
            state = UToastState.ERROR,
        ),
    ) {
        CommunityCreateMemberSheetPreviewContent(
            state = CommunityCreateMemberBottomSheetState(
                query = "김",
                isSearching = true,
                searchResults = previewChallengers,
                selectedMembers = previewChallengers.take(1).toImmutableList(),
            ),
            modifier = Modifier.weight(1f),
            isSearchFieldFocused = true,
        )
    }
}
