package com.umc.presentation.study.admin.group.schedule.bottomsheet

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

/**
 * 스터디 일정에 초대할 챌린저를 선택하는 BottomSheet
 *
 * 주요 기능
 * - 기존 선택 챌린저 표시
 * - 이름 기반 챌린저 검색
 * - 검색 결과 파트별 표시
 * - 챌린저 선택 및 선택 해제
 * - 선택된 챌린저 삭제
 * - 검색 결과 페이지네이션
 * - 사용자 프로필 이미지 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScheduleChallengerBottomSheet(
    viewModel: GroupScheduleChallengerViewModel = hiltViewModel(),
    preSelected: ImmutableList<GroupScheduleChallengerUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (
        List<GroupScheduleChallengerUiModel>,
        String,
    ) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    /**
     * BottomSheet를 닫을 때 현재 선택 결과를
     * 상위 화면으로 전달합니다.
     */
    fun dismissWithApply() {
        onConfirm(
            state.selectedChallengers,
            state.selectedSummaryText
        )

        viewModel.resetAfterConfirm()
        onDismissRequest()
    }

    /**
     * BottomSheet가 열릴 때 기존 선택 목록을 초기화합니다.
     */
    LaunchedEffect(preSelected) {
        viewModel.resetAfterConfirm()
        viewModel.setSelected(preSelected)
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
            // 검색 여부에 따라 제목과 확인 버튼 표시
            GroupScheduleChallengerHeader(
                title = if (state.isSearching) {
                    "초대할 챌린저를 검색하세요"
                } else {
                    "초대할 챌린저를 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled =
                    state.selectedChallengers.isNotEmpty(),
                onConfirmClick = {
                    viewModel.clearSearchOnly()
                }
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // 챌린저 검색창
            UTextField(
                value = state.query,
                onValueChange =
                    viewModel::searchChallengers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(
                    id = R.drawable.ic_search
                ),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (!state.isSearching) {

                    // 검색 전: 현재 선택된 챌린저 표시
                    if (state.selectedChallengers.isEmpty()) {
                        GroupScheduleEmptyChallengerContent()
                    } else {
                        GroupScheduleSelectedChallengerList(
                            challengers =
                                state.selectedChallengers,
                            onRemoveClick =
                                viewModel::toggleChallenger
                        )
                    }
                } else {

                    // 검색 중: 검색 결과 표시
                    GroupScheduleSearchChallengerList(
                        searchResults =
                            state.searchResults,
                        selectedChallengers =
                            state.selectedChallengers,
                        isLoading =
                            state.isLoading,
                        hasNext =
                            state.hasNext,
                        onToggleClick =
                            viewModel::toggleChallenger,
                        onLoadMore =
                            viewModel::loadMoreChallengers,
                    )
                }

                // API 검색 로딩
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
 * 챌린저 선택 BottomSheet의 상단 헤더
 */
@Composable
fun GroupScheduleChallengerHeader(
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

        // 검색 중일 때만 확인 버튼 표시
        if (showConfirmButton) {
            UButton(
                text = "확인",
                onClick = onConfirmClick,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp),
                backgroundColor =
                    if (isConfirmEnabled) {
                        indigo500()
                    } else {
                        grey100()
                    },
                textColor =
                    if (isConfirmEnabled) {
                        grey000()
                    } else {
                        grey400()
                    },
                textStyle =
                    UmcTypographyTokens.SubheadlineBold,
                cornerRadius = 8.dp
            )
        }
    }
}

/**
 * 아직 초대한 챌린저가 없을 때 표시하는 빈 화면
 */
@Composable
fun GroupScheduleEmptyChallengerContent() {
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
            text = "아직 초대한 챌린저가 없어요",
            style = UmcTypographyTokens.Body,
            color = grey600()
        )
    }
}

/**
 * 현재 선택된 챌린저 목록
 */
@Composable
fun GroupScheduleSelectedChallengerList(
    challengers: ImmutableList<GroupScheduleChallengerUiModel>,
    onRemoveClick: (
        GroupScheduleChallengerUiModel
    ) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = challengers,
            key = { challenger ->
                challenger.id
            }
        ) { item ->
            GroupScheduleAddedChallengerRow(
                item = item,
                onRemoveClick = {
                    onRemoveClick(item)
                }
            )
        }
    }
}

/**
 * 현재 선택된 개별 챌린저 항목
 *
 * 프로필 이미지, 이름, 학교 정보를 표시하며
 * 삭제 버튼으로 초대 대상에서 제거할 수 있습니다.
 */
@Composable
fun GroupScheduleAddedChallengerRow(
    item: GroupScheduleChallengerUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사용자 프로필 이미지
        GroupScheduleChallengerProfile(
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
                style =
                    UmcTypographyTokens.SubheadlineBold,
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

        // 초대 대상에서 제거
        UButton(
            text = "삭제",
            onClick = onRemoveClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor =
                red500().copy(alpha = 0.12f),
            textColor = red500(),
            textStyle =
                UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp
        )
    }
}

/**
 * 챌린저 검색 결과 목록
 *
 * 검색 결과를 파트별로 그룹화하고,
 * 현재 선택 여부를 체크박스로 표시합니다.
 */
@Composable
fun GroupScheduleSearchChallengerList(
    searchResults: ImmutableList<GroupScheduleChallengerUiModel>,
    selectedChallengers: ImmutableList<GroupScheduleChallengerUiModel>,
    isLoading: Boolean,
    hasNext: Boolean,
    onToggleClick: (
        GroupScheduleChallengerUiModel
    ) -> Unit,
    onLoadMore: () -> Unit,
) {
    val groupedResults = searchResults
        .groupBy { challenger ->
            challenger.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        groupedResults.forEach {
                (partLabel, challengers) ->

            // 파트별 검색 결과 제목
            if (partLabel.isNotBlank()) {
                item(
                    key = "part_header_$partLabel"
                ) {
                    UText(
                        text = partLabel,
                        style =
                            UmcTypographyTokens.BodyBold,
                        color = grey900(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp,
                                bottom = 8.dp,
                            )
                    )
                }
            }

            items(
                items = challengers,
                key = { challenger ->
                    challenger.id
                }
            ) { item ->
                val isChecked =
                    selectedChallengers.any { selected ->
                        selected.id == item.id
                    }

                GroupScheduleSearchChallengerRow(
                    item = item,
                    isChecked = isChecked,
                    onToggleClick = {
                        onToggleClick(item)
                    },
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

        /**
         * 다음 페이지가 존재하면
         * 리스트 하단에서 추가 검색 결과를 조회합니다.
         */
        if (
            hasNext &&
            searchResults.isNotEmpty() &&
            !isLoading
        ) {
            item(
                key = "load_more"
            ) {
                LaunchedEffect(
                    searchResults.size,
                    hasNext,
                ) {
                    onLoadMore()
                }
            }
        }
    }
}

/**
 * 챌린저 검색 결과의 개별 사용자 항목
 */
@Composable
fun GroupScheduleSearchChallengerRow(
    item: GroupScheduleChallengerUiModel,
    isChecked: Boolean,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onToggleClick()
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 검색 결과 사용자 프로필 이미지
        GroupScheduleChallengerProfile(
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
                style =
                    UmcTypographyTokens.SubheadlineBold,
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

        // 선택 여부 체크박스
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

/**
 * 일정 초대 챌린저의 프로필 이미지
 *
 * profileImageUrl이 존재하면 서버 이미지를 표시하고,
 * 이미지가 없으면 기본 프로필 아이콘을 표시합니다.
 */
@Composable
fun GroupScheduleChallengerProfile(
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