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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import com.umc.component.component.UText
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.grey000
import com.umc.presentation.community.component.CommunityErrorContent
import com.umc.presentation.community.component.CommunityLoadingContent
import com.umc.presentation.community.component.CommunityThreadItem
import com.umc.presentation.community.component.search.CommunityRecentSearchContent
import com.umc.presentation.community.component.search.CommunitySearchBar
import com.umc.presentation.community.component.search.CommunitySearchEmptyContent
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

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
    val coroutineScope = rememberCoroutineScope()

    // 검색 화면에 진입하면 검색창에 자동 포커스하고 키보드를 표시
    // 결과를 보던 중 스레드에 들어갔다 돌아온 경우에는 결과 위로 키보드를 다시 띄우지 않는다
    LaunchedEffect(Unit) {
        if (state.hasSearched) return@LaunchedEffect
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
                    // 키보드만 내리고 포커스는 검색창에 둔다.
                    // clearFocus() 는 ComposeView 의 View 포커스까지 지우는데, 하드웨어 키(Enter)로 검색해
                    // 터치 모드가 아닐 때는 Android 가 곧바로 포커스를 되돌려 검색창이 다시 포커스를 받고
                    // 키보드가 다시 올라온다.
                    keyboardController?.hide()

                    onAction(CommunitySearchAction.OnSearchClick)
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

            // 검색 응답을 기다리는 중에는 결과 없음 문구 대신 로딩 표시
            state.hasSearched && state.isLoading && state.searchResults.isEmpty() -> {
                CommunityLoadingContent()
            }

            // 검색 요청이 실패했을 때는 결과 없음과 구분해 다시 시도 제공
            state.hasSearched && state.errorMessage != null && state.searchResults.isEmpty() -> {
                CommunityErrorContent(
                    errorMessage = state.errorMessage,
                    onRetryClick = {
                        keyboardController?.hide()
                        onAction(CommunitySearchAction.OnRetryClick)
                    },
                    modifier = Modifier.fillMaxSize(),
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
                    onLoadMore = { onAction(CommunitySearchAction.OnLoadMore) },
                    // onRefresh = { onAction(CommunitySearchAction.OnSearchClick) },
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
    onLoadMore: () -> Unit,
    // onRefresh: () -> Unit,
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
        item(key = "search_refresh") {
            // 검색 결과 새로고침 버튼 임시 비활성화.
            // TextButton(onClick = onRefresh, enabled = !state.isLoading) { UText(if (state.isLoading) "새로고침 중" else "새로고침") }
            state.errorMessage?.let { message ->
                UText(message)
            }
        }
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
        item(key = "search_pagination") {
            when {
                state.isLoadingMore -> CircularProgressIndicator(Modifier.padding(16.dp))
                state.loadMoreError != null -> Column {
                    UText(state.loadMoreError)
                    TextButton(onClick = onLoadMore) { UText("다시 시도") }
                }
                state.nextOffset != null -> TextButton(onClick = onLoadMore) {
                    UText("검색 결과 더 보기")
                }
            }
        }
    }
}

/**
 * 검색 응답 대기 중 Preview
 */
@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
private fun CommunitySearchScreenLoadingPreview() {
    UmcTheme {
        CommunitySearchScreen(
            state = CommunitySearchState(
                query = "스터디",
                hasSearched = true,
                isLoading = true,
            ),
            onAction = {},
        )
    }
}

/**
 * 검색 요청 실패 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunitySearchScreenErrorPreview() {
    UmcTheme {
        CommunitySearchScreen(
            state = CommunitySearchState(
                query = "스터디",
                hasSearched = true,
                errorMessage = "네트워크 연결을 확인하고 다시 시도해주세요.",
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunitySearchScreenTypingPreview() {
    UmcTheme {
        CommunitySearchScreen(
            state = CommunitySearchState(query = "스터디", hasSearched = false),
            onAction = {},
        )
    }
}
