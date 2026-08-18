package com.umc.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey950
import com.umc.presentation.community.component.CommunityEmptyContent
import com.umc.presentation.community.component.CommunityErrorContent
import com.umc.presentation.community.component.CommunityFloatingButton
import com.umc.presentation.community.component.CommunityLoadingContent
import com.umc.presentation.community.component.CommunityThreadItem
import com.umc.presentation.community.component.CommunityTopBar
import com.umc.presentation.community.dialog.CommunityLeaveDialog
import com.umc.presentation.community.dialog.CommunityThreadMenuDialog

/**
 * 커뮤니티 메인 화면
 *
 * 스레드 목록과 카테고리 필터를 표시하고,
 * 검색/생성/스레드 관리 기능을 제공합니다.
 */
@Composable
fun CommunityScreen(
    state: CommunityState,
    onAction: (CommunityAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 카테고리 필터 Popup 표시 여부
    var isFilterMenuExpanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 커뮤니티 상단바 및 카테고리 필터
            CommunityTopBar(
                isFilterMenuExpanded = isFilterMenuExpanded,
                selectedCategory = state.selectedCategory,
                onFilterClick = {
                    isFilterMenuExpanded = !isFilterMenuExpanded
                    onAction(CommunityAction.OnFilterClick)
                },
                onFilterDismissRequest = {
                    isFilterMenuExpanded = false
                },
                onCategorySelected = { category ->
                    onAction(
                        CommunityAction.OnCategorySelected(category)
                    )
                },
                onSearchClick = {
                    onAction(CommunityAction.OnSearchClick)
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                // 목록 상태에 따라 로딩/에러/Empty/스레드 목록 표시
                when {
                    state.isLoading -> {
                        CommunityLoadingContent()
                    }

                    state.isError -> {
                        CommunityErrorContent(
                            errorMessage = state.errorMessage.orEmpty(),
                            onRetryClick = {
                                onAction(CommunityAction.OnRetryClick)
                            },
                        )
                    }

                    state.isEmpty -> {
                        CommunityEmptyContent()
                    }

                    else -> {
                        CommunityThreadList(
                            state = state,
                            onThreadClick = { threadId ->
                                onAction(
                                    CommunityAction.OnThreadClick(threadId)
                                )
                            },
                            onThreadLongClick = { threadId ->
                                onAction(
                                    CommunityAction.OnThreadLongClick(threadId)
                                )
                            },
                        )
                    }
                }
            }
        }

        // 로딩 또는 에러 상태가 아닐 때 새 스레드 생성 버튼 표시
        if (!state.isLoading && !state.isError) {
            CommunityFloatingButton(
                onClick = {
                    onAction(CommunityAction.OnCreateThreadClick)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
            )
        }

        // 스레드 롱클릭 시 관리 메뉴 표시
        if (
            state.showThreadMenuDialog &&
            state.selectedThread != null
        ) {
            CommunityThreadMenuDialog(
                thread = state.selectedThread,
                onDismissRequest = {
                    onAction(CommunityAction.OnDismissThreadMenu)
                },
                onTogglePinClick = {
                    onAction(CommunityAction.OnTogglePinClick)
                },
                onToggleNotificationClick = {
                    onAction(
                        CommunityAction.OnToggleNotificationClick
                    )
                },
                onEditClick = {
                    onAction(CommunityAction.OnEditThreadClick)
                },
                onLeaveClick = {
                    onAction(CommunityAction.OnLeaveThreadClick)
                },
            )
        }

        // 스레드 나가기 확인 Dialog
        if (state.showLeaveDialog) {
            CommunityLeaveDialog(
                onDismissRequest = {
                    onAction(CommunityAction.OnDismissLeaveDialog)
                },
                onConfirmClick = {
                    onAction(CommunityAction.OnConfirmLeaveClick)
                },
            )
        }
    }
}

/**
 * 고정 스레드와 일반 스레드 목록을 구분하여 표시합니다.
 */
@Composable
private fun CommunityThreadList(
    state: CommunityState,
    onThreadClick: (String) -> Unit,
    onThreadLongClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 96.dp,
        ),
    ) {
        // 고정된 스레드 목록
        if (state.pinnedThreads.isNotEmpty()) {
            item(
                key = "pinned_title",
            ) {
                CommunitySectionTitle(
                    text = "고정",
                )
            }

            items(
                items = state.pinnedThreads,
                key = { thread ->
                    "pinned_${thread.id}"
                },
            ) { thread ->
                CommunityThreadItem(
                    thread = thread,
                    onClick = {
                        onThreadClick(thread.id)
                    },
                    onLongClick = {
                        onThreadLongClick(thread.id)
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            item(
                key = "normal_section_space",
            ) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // 현재 선택한 카테고리의 일반 스레드 목록
        item(
            key = "normal_title_${state.selectedCategory.name}",
        ) {
            CommunitySectionTitle(
                text = state.selectedSectionTitle,
            )
        }

        items(
            items = state.normalThreads,
            key = { thread ->
                "normal_${thread.id}"
            },
        ) { thread ->
            CommunityThreadItem(
                thread = thread,
                onClick = {
                    onThreadClick(thread.id)
                },
                onLongClick = {
                    onThreadLongClick(thread.id)
                },
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

/**
 * 스레드 목록의 섹션 제목을 표시합니다.
 */
@Composable
private fun CommunitySectionTitle(
    text: String,
) {
    UText(
        text = text,
        style = UmcTypographyTokens.HeadlineBold,
        color = grey950(),
        modifier = Modifier.padding(8.dp),
    )
}