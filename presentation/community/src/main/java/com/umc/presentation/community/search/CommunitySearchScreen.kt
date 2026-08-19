package com.umc.presentation.community.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.umc.component.theme.grey000
import com.umc.presentation.community.component.CommunityThreadItem
import com.umc.presentation.community.component.search.CommunityRecentSearchContent
import com.umc.presentation.community.component.search.CommunitySearchBar
import com.umc.presentation.community.component.search.CommunitySearchEmptyContent
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalFocusManager

/**
 * 커뮤니티 스레드 검색 화면
 *
 * 검색어 입력, 최근 검색어 표시 및 관리,
 * 검색 결과 목록과 검색 결과 없음 화면을 구성합니다.
 */
@Composable
fun CommunitySearchScreen(
    state: CommunitySearchState,
    onAction: (CommunitySearchAction) -> Unit,
    modifier: Modifier = Modifier,

) {
    val focusRequester = remember {
        FocusRequester()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // 검색 화면에 진입하면 검색창에 자동 포커스하고 키보드를 표시
    LaunchedEffect(Unit) {
        delay(100L)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        CommunitySearchBar(
            query = state.query,
            hasSearched = state.hasSearched,
            focusRequester = focusRequester,
            onQueryChanged = { query ->
                onAction(
                    CommunitySearchAction.OnQueryChanged(query)
                )
            },
            onSearchClick = {
                if (state.query.isNotBlank()) {
                    // 검색을 먼저 실행
                    onAction(CommunitySearchAction.OnSearchClick)

                    // 그다음 키보드와 포커스 정리
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            },
            onClearClick = {
                onAction(CommunitySearchAction.OnClearQueryClick)

                focusRequester.requestFocus()
                keyboardController?.show()
            },
            onCancelClick = {
                keyboardController?.hide()
                onAction(CommunitySearchAction.OnCancelClick)
            },
            onBackClick = {
                keyboardController?.hide()
                onAction(CommunitySearchAction.OnBackClick)
            },
            modifier = Modifier.padding(
                start = if (state.hasSearched) 8.dp else 16.dp,
                end = 16.dp,
                top = 12.dp,
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            // 검색 전이면서 검색창이 비어 있을 때만 최근 검색어 표시
            !state.hasSearched && state.query.isBlank() -> {
                CommunityRecentSearchContent(
                    recentSearches = state.recentSearches,
                    onRecentSearchClick = { recentSearch ->
                        keyboardController?.hide()

                        onAction(
                            CommunitySearchAction.OnRecentSearchClick(
                                query = recentSearch,
                            )
                        )
                    },
                    onDeleteRecentSearchClick = { recentSearch ->
                        onAction(
                            CommunitySearchAction.OnDeleteRecentSearchClick(
                                query = recentSearch,
                            )
                        )
                    },
                    onClearAllClick = {
                        onAction(
                            CommunitySearchAction.OnClearAllRecentSearchesClick
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // 검색을 실행했고 결과가 없을 때
            state.hasSearched && state.searchResults.isEmpty() -> {
                CommunitySearchEmptyContent(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 검색을 실행했고 결과가 있을 때
            state.hasSearched -> {
                CommunitySearchResultList(
                    state = state,
                    onThreadClick = { threadId ->
                        onAction(
                            CommunitySearchAction.OnThreadClick(threadId)
                        )
                    },
                )
            }

            // 검색어를 입력 중인 상태
            else -> {
                Spacer(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 검색된 스레드 목록을 표시하는 영역
 */
@Composable
private fun CommunitySearchResultList(
    state: CommunitySearchState,
    onThreadClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp,
        ),
    ) {
        items(
            items = state.searchResults,
            key = { thread ->
                thread.id
            },
        ) { thread ->
            CommunityThreadItem(
                thread = thread,
                onClick = {
                    onThreadClick(thread.id)
                },
                onLongClick = {},
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}