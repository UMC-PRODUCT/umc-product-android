package com.umc.presentation.notice.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.umc.component.theme.grey600
import com.umc.component.theme.indigo500
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.presentation.notice.NoticeCard
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

@Composable
fun NoticeSearchRoute(
    gisuId: Long = 0,
    noticeTab: String = "CHALLENGER",
    chapterId: Long? = null,
    schoolId: Long? = null,
    part: String? = null,
    viewModel: NoticeSearchViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToDetail: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gisuId, noticeTab, chapterId, schoolId, part) {
        viewModel.setSearchContext(gisuId, noticeTab, chapterId, schoolId, part)
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is NoticeSearchEvent.MoveToDetailEvent -> navigateToDetail(event.noticeId)
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
    val keyboardController = LocalSoftwareKeyboardController.current

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
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // 결과가 키보드에 가리지 않도록 내린다
                        keyboardController?.hide()
                        onSearch()
                    },
                ),
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

        // 결과가 없을 때 아무것도 안 그리면 "검색이 안 된다"로 보인다.
        // 로딩 / 0건 / 실패를 구분해서 알려준다
        when {
            uiState.isPageLoading && uiState.resultList.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = indigo500())
                }
            }

            uiState.errorMessage != null && uiState.resultList.isEmpty() -> {
                NoticeSearchMessage(
                    modifier = Modifier.weight(1f),
                    title = AppStrings.NOTICE_SEARCH_ERROR,
                    content = uiState.errorMessage,
                )
            }

            uiState.resultList.isEmpty() -> {
                NoticeSearchMessage(
                    modifier = Modifier.weight(1f),
                    title = AppStrings.NOTICE_SEARCH_EMPTY_TITLE,
                    content = AppStrings.NOTICE_SEARCH_EMPTY_CONTENT,
                )
            }

            else -> {
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
                    if (!uiState.isLastPage) {
                        item {
                            LaunchedEffect(Unit) {
                                onLoadNextPage()
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 검색 결과 영역의 안내 문구 (0건 / 오류) */
@Composable
private fun NoticeSearchMessage(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        UText(
            text = content,
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeSearchScreenPreview() {
    NoticeSearchScreen(
        uiState = NoticeSearchUiState(
            recentSearchList = persistentListOf("중앙", "해커톤"),
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
