package com.umc.presentation.notice.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.presentation.notice.NoticeCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NoticeSearchRoute(
    gisuId: Long = 0,
    viewModel: NoticeSearchViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToDetail: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setGisuId(gisuId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoticeSearchEvent.MoveToDetailEvent -> navigateToDetail(event.noticeId)
            }
        }
    }

    if (uiState.isResultMode) {
        NoticeSearchResultScreen(
            uiState = uiState,
            onClickBack = viewModel::backToSearchMode,
            onClickField = viewModel::backToSearchMode,
            onClickNotice = viewModel::onClickNotice,
            onLoadNextPage = viewModel::loadNextPage,
        )
    } else {
        NoticeSearchScreen(
            uiState = uiState,
            onQueryChanged = viewModel::onQueryChanged,
            onSearch = viewModel::onSearch,
            onClickCancel = navigateToBack,
            onClickRecentSearch = viewModel::selectRecentSearch,
            onClickDeleteRecent = viewModel::deleteRecentSearch,
            onClickDeleteAll = viewModel::deleteAllRecentSearch,
        )
    }
}

/** 검색어 입력 화면 (최근 검색어 목록) */
@Composable
fun NoticeSearchScreen(
    uiState: NoticeSearchUiState = NoticeSearchUiState(),
    onQueryChanged: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onClickCancel: () -> Unit = {},
    onClickRecentSearch: (String) -> Unit = {},
    onClickDeleteRecent: (String) -> Unit = {},
    onClickDeleteAll: () -> Unit = {},
) {
    val searchFocusRequester = remember { FocusRequester() }

    // 진입 시 검색 필드에 포커스
    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                placeholder = AppStrings.NOTICE_SEARCH_PLACEHOLDER,
                textStyle = UmcTypographyTokens.Headline,
                textColor = grey950(),
                strokeColor = grey200(),
                focusStrokeColor = grey900(),
                cornerRadius = 12.dp,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                // 아이콘을 항상 배치해 필드 높이를 고정하고, 입력값이 없을 땐 투명 처리로 숨김
                nextIcon = painterResource(id = R.drawable.ic_delete_filled),
                nextIconTint = if (uiState.query.isNotEmpty()) grey300() else Color.Transparent,
                onClickNextIcon = if (uiState.query.isNotEmpty()) {
                    { onQueryChanged("") }
                } else {
                    null
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(searchFocusRequester),
            )

            Spacer(modifier = Modifier.width(16.dp))

            UText(
                text = AppStrings.CANCEL,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
                modifier = Modifier.clickable { onClickCancel() },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UText(
                text = AppStrings.NOTICE_SEARCH_RECENT,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.weight(1f))

            UText(
                text = AppStrings.NOTICE_SEARCH_DELETE_ALL,
                style = UmcTypographyTokens.Subheadline,
                color = grey500(),
                modifier = Modifier.clickable { onClickDeleteAll() },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(
                items = uiState.recentSearchList,
                key = { it },
            ) { keyword ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickRecentSearch(keyword) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UText(
                        text = keyword,
                        style = UmcTypographyTokens.Body,
                        color = grey800(),
                        modifier = Modifier.weight(1f),
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                        tint = grey400(),
                        modifier = Modifier.clickable { onClickDeleteRecent(keyword) },
                    )
                }
            }
        }
    }
}

/** 검색 결과 화면 */
@Composable
fun NoticeSearchResultScreen(
    uiState: NoticeSearchUiState = NoticeSearchUiState(),
    onClickBack: () -> Unit = {},
    onClickField: () -> Unit = {},
    onClickNotice: (Long) -> Unit = {},
    onLoadNextPage: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { onClickBack() },
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, grey200(), RoundedCornerShape(12.dp))
                    .clickable { onClickField() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                UText(
                    text = uiState.query,
                    style = UmcTypographyTokens.Headline,
                    color = grey950(),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = uiState.resultList,
                key = { it.id },
            ) { notice ->
                NoticeCard(
                    notice = notice,
                    isRead = uiState.readNoticeIds.contains(notice.id),
                    onClick = { onClickNotice(notice.id) },
                )
            }

            // 마지막 아이템 노출 시 다음 페이지 로드
            if (uiState.resultList.isNotEmpty() && !uiState.isLastPage) {
                item {
                    LaunchedEffect(Unit) {
                        onLoadNextPage()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeSearchScreenPreview() {
    NoticeSearchScreen(
        uiState = NoticeSearchUiState(
            recentSearchList = listOf("중앙", "해커톤"),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoticeSearchResultScreenPreview() {
    NoticeSearchResultScreen(
        uiState = NoticeSearchUiState(
            query = "중앙 해커톤",
            isResultMode = true,
        ),
    )
}
