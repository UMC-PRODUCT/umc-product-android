package com.umc.presentation.notice.adminnotice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey700
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.presentation.notice.NoticeCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdminNoticeRoute(
    gisuId: Long = 0,
    viewModel: AdminNoticeViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToSearch: (Long, String, Long?) -> Unit = { _, _, _ -> },
    navigateToDetail: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setGisuId(gisuId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AdminNoticeEvent.MoveToSearchEvent ->
                    navigateToSearch(event.gisuId, event.noticeTab, event.schoolId)
                is AdminNoticeEvent.MoveToDetailEvent -> navigateToDetail(event.noticeId)
            }
        }
    }

    AdminNoticeScreen(
        uiState = uiState,
        onClickBack = navigateToBack,
        onClickSearch = viewModel::onClickSearch,
        onClickTab = viewModel::onClickTab,
        onClickNotice = viewModel::onClickNotice,
        onLoadNextPage = viewModel::loadNextPage,
    )
}

@Composable
fun AdminNoticeScreen(
    uiState: AdminNoticeUiState = AdminNoticeUiState(),
    onClickBack: () -> Unit = {},
    onClickSearch: () -> Unit = {},
    onClickTab: (AdminNoticeTab) -> Unit = {},
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
                .padding(start = 8.dp, end = 20.dp, top = 8.dp),
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

            UText(
                text = uiState.selectedTab?.label.orEmpty(),
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = grey950(),
                modifier = Modifier.clickable { onClickSearch() },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 권한 계층에 따라 노출되는 운영진 공지 탭
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = uiState.visibleTabs,
                key = { it.name },
            ) { tab ->
                AdminNoticeChip(
                    text = tab.label,
                    isSelected = tab == uiState.selectedTab,
                    onClick = { onClickTab(tab) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            !uiState.hasAccess -> {
                AdminNoticeStatusContent(
                    iconRes = R.drawable.ic_locked_filled,
                    title = AppStrings.NOTICE_NO_ACCESS_TITLE,
                    content = AppStrings.NOTICE_NO_ACCESS_CONTENT,
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.isEmpty -> {
                AdminNoticeStatusContent(
                    // TODO: 확성기 아이콘 에셋 추가 후 교체 (임시로 ic_alarm 사용)
                    iconRes = R.drawable.ic_speaker,
                    title = AppStrings.NOTICE_EMPTY_TITLE,
                    content = AppStrings.NOTICE_EMPTY_CONTENT,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        items = uiState.noticeList,
                        key = { it.id },
                    ) { notice ->
                        NoticeCard(
                            notice = notice,
                            isRead = uiState.readNoticeIds.contains(notice.id),
                            onClick = { onClickNotice(notice.id) },
                        )
                    }

                    // 마지막 아이템 노출 시 다음 페이지 로드
                    if (uiState.noticeList.isNotEmpty() && !uiState.isLastPage) {
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

/** 운영진 공지 탭 칩 */
@Composable
private fun AdminNoticeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) indigo500() else grey100())
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.SubheadlineBold,
            color = if (isSelected) grey000() else grey500(),
        )
    }
}

/** 빈 상태/접근 권한 없음 안내 콘텐츠 */
@Composable
private fun AdminNoticeStatusContent(
    iconRes: Int,
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = grey300(),
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = title,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey700(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = content,
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminNoticeScreenPreview() {
    AdminNoticeScreen(
        uiState = AdminNoticeUiState(
            visibleTabs = AdminNoticeTab.entries,
            selectedTab = AdminNoticeTab.CENTRAL,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun AdminNoticeScreenNoAccessPreview() {
    AdminNoticeScreen(
        uiState = AdminNoticeUiState(
            hasAccess = false,
            visibleTabs = listOf(AdminNoticeTab.PART_LEADER),
            selectedTab = AdminNoticeTab.PART_LEADER,
        ),
    )
}
