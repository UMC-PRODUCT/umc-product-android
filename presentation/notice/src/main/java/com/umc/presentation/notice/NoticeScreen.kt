package com.umc.presentation.notice

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo600
import com.umc.component.theme.indigo700
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.GisuItem
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeRoute(
    viewModel: NoticeViewModel = hiltViewModel(),
    navigateToSearch: (Long) -> Unit = {},
    navigateToAdminNotice: (Long) -> Unit = {},
    navigateToWrite: () -> Unit = {},
    navigateToDetail: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPartBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoticeEvent.MoveToSearchEvent -> navigateToSearch(event.gisuId)
                is NoticeEvent.MoveToAdminNoticeEvent -> navigateToAdminNotice(event.gisuId)
                is NoticeEvent.MoveToWriteEvent -> navigateToWrite()
                is NoticeEvent.MoveToDetailEvent -> navigateToDetail(event.noticeId)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NoticeScreen(
            uiState = uiState,
            onClickShowDropDown = viewModel::onClickShowDropDown,
            onClickGisu = viewModel::onClickGisu,
            onClickSearch = viewModel::onClickSearch,
            onClickAdminNotice = viewModel::onClickAdminNotice,
            onClickOrgChip = viewModel::onClickOrgChip,
            onClickSubChipAll = viewModel::onClickSubChipAll,
            onClickSubChipStaff = viewModel::onClickSubChipStaff,
            onClickPartChip = { showPartBottomSheet = true },
            onClickNotice = viewModel::onClickNotice,
            onLoadNextPage = viewModel::loadNextPage,
            onClickWriteNotice = viewModel::onClickWriteNotice,
        )

        if (showPartBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPartBottomSheet = false },
                sheetState = sheetState,
                containerColor = grey000(),
            ) {
                PartSelectBottomSheetContent(
                    onSelectPart = { part ->
                        viewModel.onSelectPart(part)
                        showPartBottomSheet = false
                    },
                )
            }
        }
    }
}

@Composable
fun NoticeScreen(
    uiState: NoticeUiState = NoticeUiState(),
    onClickShowDropDown: () -> Unit = {},
    onClickGisu: (GisuItem) -> Unit = {},
    onClickSearch: () -> Unit = {},
    onClickAdminNotice: () -> Unit = {},
    onClickOrgChip: (NoticeChipState) -> Unit = {},
    onClickSubChipAll: () -> Unit = {},
    onClickSubChipStaff: () -> Unit = {},
    onClickPartChip: () -> Unit = {},
    onClickNotice: (Long) -> Unit = {},
    onLoadNextPage: () -> Unit = {},
    onClickWriteNotice: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NoticeHeader(
                uiState = uiState,
                onClickShowDropDown = onClickShowDropDown,
                onClickGisu = onClickGisu,
                onClickSearch = onClickSearch,
                onClickAdminNotice = onClickAdminNotice,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1차 필터 (소속)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = uiState.orgChipList,
                    key = { it.text },
                ) { chip ->
                    NoticeFilterChip(
                        text = chip.text,
                        isSelected = chip.isClicked,
                        selectedColor = indigo500(),
                        onClick = { onClickOrgChip(chip) },
                    )
                }
            }

            // 2차 필터 (전체 / 운영진 공지 / 파트)
            if (uiState.isSubChipVisible) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(thickness = 1.dp, color = grey100())

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NoticeFilterChip(
                        text = AppStrings.ALL,
                        isSelected = uiState.selectedSubChip == NoticeSubChip.ALL,
                        selectedColor = grey950(),
                        onClick = onClickSubChipAll,
                    )

                    if (uiState.canWriteNotice) {
                        NoticeFilterChip(
                            text = AppStrings.NOTICE_STAFF_CHIP,
                            isSelected = uiState.selectedSubChip == NoticeSubChip.STAFF,
                            selectedColor = grey950(),
                            onClick = onClickSubChipStaff,
                        )
                    }

                    NoticeFilterChip(
                        text = uiState.selectedPart?.label ?: AppStrings.PART,
                        isSelected = uiState.selectedSubChip == NoticeSubChip.PART,
                        selectedColor = grey950(),
                        hasDropdownIcon = true,
                        onClick = onClickPartChip,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
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

        if (uiState.canWriteNotice) {
            UButton(
                text = AppStrings.NOTICE_WRITE,
                onClick = onClickWriteNotice,
                backgroundColor = indigo500(),
                pressedColor = indigo700(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 28.dp,
                prevIcon = painterResource(id = R.drawable.ic_plus_circle),
                prevIconTint = grey000(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

/** 상단 헤더: 기수 드롭다운 타이틀 + 관리자 + 검색 아이콘 */
@Composable
private fun NoticeHeader(
    uiState: NoticeUiState,
    onClickShowDropDown: () -> Unit = {},
    onClickGisu: (GisuItem) -> Unit = {},
    onClickSearch: () -> Unit = {},
    onClickAdminNotice: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (uiState.isShowDropDown) grey100() else grey000())
                    .clickable { onClickShowDropDown() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UText(
                    text = uiState.nowTitle,
                    style = UmcTypographyTokens.Title2Bold,
                    color = grey950(),
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_dropdown_down),
                    contentDescription = null,
                    tint = grey600(),
                )
            }

            DropdownMenu(
                expanded = uiState.isShowDropDown,
                onDismissRequest = onClickShowDropDown,
                modifier = Modifier.background(grey000()),
            ) {
                uiState.dropdownList.forEach { gisu ->
                    val isSelected = gisu.gisuId.toLong() == uiState.selectedGisu

                    Row(
                        modifier = Modifier
                            .clickable { onClickGisu(gisu) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UText(
                            text = gisu.displayText,
                            style = if (isSelected) UmcTypographyTokens.HeadlineBold else UmcTypographyTokens.Headline,
                            color = if (isSelected) indigo600() else grey800(),
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(24.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_white),
                                contentDescription = null,
                                tint = indigo500(),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_guard),
            contentDescription = null,
            tint = grey950(),
            modifier = Modifier.clickable { onClickAdminNotice() },
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = grey950(),
            modifier = Modifier.clickable { onClickSearch() },
        )
    }
}

/** 필터 칩. 1차(indigo)/2차(black)는 selectedColor로 구분 */
@Composable
private fun NoticeFilterChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    hasDropdownIcon: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) selectedColor else grey100())
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.SubheadlineBold,
            color = if (isSelected) grey000() else grey500(),
        )

        if (hasDropdownIcon) {
            Spacer(modifier = Modifier.width(4.dp))

            // 텍스트 라인 높이보다 작게 고정해 텍스트만 있는 칩과 높이를 일치시킴
            Icon(
                painter = painterResource(id = R.drawable.ic_dropdown_down),
                contentDescription = null,
                tint = if (isSelected) grey000() else grey500(),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** 파트 선택 바텀시트 */
@Composable
private fun PartSelectBottomSheetContent(
    onSelectPart: (UserPart) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        UText(
            text = AppStrings.NOTICE_BOTTOMSHEET_TITLE,
            style = UmcTypographyTokens.Title3Bold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UserPart.entries
            .filter { it != UserPart.UNKNOWN && it != UserPart.ADMIN }
            .forEach { part ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPart(part) }
                        .padding(vertical = 16.dp),
                ) {
                    UText(
                        text = part.label,
                        style = UmcTypographyTokens.Body,
                        color = grey800(),
                    )
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeScreenPreview() {
    NoticeScreen(
        uiState = NoticeUiState(
            nowTitle = "12기 공지사항",
            orgChipList = listOf(
                NoticeChipState(text = "전체", isClicked = true),
                NoticeChipState(text = "중앙운영사무국"),
                NoticeChipState(text = "Ain 지부"),
                NoticeChipState(text = "중앙대학교"),
            ),
            canWriteNotice = true,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoticeScreenSubChipPreview() {
    NoticeScreen(
        uiState = NoticeUiState(
            nowTitle = "12기 공지사항",
            orgChipList = listOf(
                NoticeChipState(text = "전체"),
                NoticeChipState(text = "중앙운영사무국", isClicked = true),
            ),
            selectedOrgChipText = "중앙운영사무국",
            selectedSubChip = NoticeSubChip.PART,
            selectedPart = UserPart.PLAN,
            canWriteNotice = true,
        ),
    )
}
