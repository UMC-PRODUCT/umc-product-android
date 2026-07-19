package com.umc.presentation.notice.write

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
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
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.domain.model.enums.UserPart

/** 공지 작성 화면 바텀시트 종류 */
private enum class WriteSheetType { CATEGORY, CHAPTER, SCHOOL, PART }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeWriteRoute(
    viewModel: NoticeWriteViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var sheetType by remember { mutableStateOf<WriteSheetType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    NoticeWriteScreen(
        uiState = uiState,
        onClickBack = navigateToBack,
        onToggleNotification = viewModel::onToggleNotification,
        onClickRegister = viewModel::onClickRegister,
        onClickCategory = { sheetType = WriteSheetType.CATEGORY },
        onToggleAll = viewModel::onToggleAll,
        onToggleStaff = viewModel::onToggleStaff,
        onClickChapterChip = { sheetType = WriteSheetType.CHAPTER },
        onClickSchoolChip = { sheetType = WriteSheetType.SCHOOL },
        onClickPartChip = { sheetType = WriteSheetType.PART },
        onTitleChanged = viewModel::onTitleChanged,
        onContentChanged = viewModel::onContentChanged,
    )

    sheetType?.let { type ->
        ModalBottomSheet(
            onDismissRequest = { sheetType = null },
            sheetState = sheetState,
            containerColor = grey000(),
        ) {
            when (type) {
                WriteSheetType.CATEGORY -> WriteSelectSheetContent(
                    title = AppStrings.NOTICE_WRITE_CATEGORY_PLACEHOLDER,
                    items = uiState.availableCategories.map { it.label },
                    onSelect = { index ->
                        viewModel.onSelectCategory(uiState.availableCategories[index])
                        sheetType = null
                    },
                )

                WriteSheetType.CHAPTER -> WriteSelectSheetContent(
                    title = AppStrings.NOTICE_WRITE_CHAPTER_SELECT,
                    items = uiState.chapterList.map { it.name },
                    onSelect = { index ->
                        viewModel.onSelectChapter(uiState.chapterList[index])
                        sheetType = null
                    },
                )

                WriteSheetType.SCHOOL -> WriteSelectSheetContent(
                    title = AppStrings.SIGN_UP_SELECT_SCHOOL_PLACEHOLDER,
                    items = uiState.schoolList.map { it.schoolName },
                    onSelect = { index ->
                        viewModel.onSelectSchool(uiState.schoolList[index])
                        sheetType = null
                    },
                )

                WriteSheetType.PART -> {
                    val parts = UserPart.entries.filter { it != UserPart.UNKNOWN && it != UserPart.ADMIN }
                    WriteSelectSheetContent(
                        title = AppStrings.NOTICE_BOTTOMSHEET_TITLE,
                        items = parts.map { it.label },
                        onSelect = { index ->
                            viewModel.onSelectPart(parts[index])
                            sheetType = null
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoticeWriteScreen(
    uiState: NoticeWriteUiState = NoticeWriteUiState(),
    onClickBack: () -> Unit = {},
    onToggleNotification: () -> Unit = {},
    onClickRegister: () -> Unit = {},
    onClickCategory: () -> Unit = {},
    onToggleAll: () -> Unit = {},
    onToggleStaff: () -> Unit = {},
    onClickChapterChip: () -> Unit = {},
    onClickSchoolChip: () -> Unit = {},
    onClickPartChip: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onContentChanged: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 22.dp, top = 8.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = AppStrings.NOTICE_WRITE_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.weight(1f))

            // 알림 발송 토글 (ON: 종 / OFF: 알림 해제 종)
            Icon(
                painter = painterResource(
                    id = if (uiState.sendNotification) R.drawable.ic_alarm_filled else R.drawable.ic_alarm_off
                ),
                contentDescription = null,
                tint = if (uiState.sendNotification) grey600() else grey300(),
                modifier = Modifier.clickable { onToggleNotification() },
            )

            Spacer(modifier = Modifier.width(18.dp))

            UText(
                text = AppStrings.REGISTER,
                style = UmcTypographyTokens.HeadlineBold,
                color = if (uiState.enableRegister) indigo500() else grey400(),
                modifier = Modifier.clickable(enabled = uiState.enableRegister) { onClickRegister() },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            UText(
                text = AppStrings.NOTICE_WRITE_CATEGORY_TITLE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 공지 카테고리 셀렉터
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, grey200(), RoundedCornerShape(8.dp))
                    .clickable { onClickCategory() }
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UText(
                    text = uiState.selectedCategory?.label
                        ?: AppStrings.NOTICE_WRITE_CATEGORY_PLACEHOLDER,
                    style = UmcTypographyTokens.Headline,
                    color = if (uiState.selectedCategory == null) grey400() else grey800(),
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    tint = grey400(),
                )
            }

            // 게시판 분류 (권한/카테고리에 따라 노출)
            if (uiState.boardChips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    UText(
                        text = AppStrings.NOTICE_WRITE_CLASS_TITLE,
                        style = UmcTypographyTokens.HeadlineBold,
                        color = grey950(),
                    )

                    uiState.boardHint?.let { hint ->
                        Spacer(modifier = Modifier.width(8.dp))

                        UText(
                            text = hint,
                            style = UmcTypographyTokens.Footnote,
                            color = grey600(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.boardChips.forEach { chipType ->
                        when (chipType) {
                            BoardChipType.ALL -> WriteBoardChip(
                                text = AppStrings.ALL,
                                isSelected = uiState.isAllSelected,
                                onClick = onToggleAll,
                            )

                            BoardChipType.STAFF -> WriteBoardChip(
                                text = AppStrings.NOTICE_STAFF_CHIP,
                                isSelected = uiState.isStaffSelected,
                                onClick = onToggleStaff,
                            )

                            BoardChipType.PART -> WriteBoardChip(
                                text = uiState.selectedPart?.label ?: AppStrings.PART,
                                isSelected = uiState.selectedPart != null,
                                hasDropdownIcon = true,
                                onClick = onClickPartChip,
                            )

                            BoardChipType.CHAPTER -> WriteBoardChip(
                                text = uiState.selectedChapter?.name ?: AppStrings.BRANCH,
                                isSelected = uiState.selectedChapter != null,
                                hasDropdownIcon = true,
                                onClick = onClickChapterChip,
                            )

                            BoardChipType.SCHOOL -> WriteBoardChip(
                                text = uiState.selectedSchool?.schoolName ?: AppStrings.SCHOOL,
                                isSelected = uiState.selectedSchool != null,
                                hasDropdownIcon = true,
                                onClick = onClickSchoolChip,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 제목 입력
            BasicTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                textStyle = UmcTypographyTokens.Title3Bold.copy(color = grey950()),
                cursorBrush = SolidColor(grey950()),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.title.isEmpty()) {
                            UText(
                                text = AppStrings.NOTICE_WRITE_TITLE_PLACEHOLDER,
                                style = UmcTypographyTokens.Title3Bold,
                                color = grey400(),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = grey200())

            Spacer(modifier = Modifier.height(16.dp))

            // 내용 입력. 내용이 길어지면 화면 전체가 아래로 스크롤됨
            BasicTextField(
                value = uiState.content,
                onValueChange = onContentChanged,
                textStyle = UmcTypographyTokens.Subheadline.copy(color = grey950()),
                cursorBrush = SolidColor(grey950()),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.content.isEmpty()) {
                            UText(
                                text = AppStrings.NOTICE_WRITE_CONTENT_PLACEHOLDER,
                                style = UmcTypographyTokens.Subheadline,
                                color = grey400(),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        HorizontalDivider(thickness = 1.dp, color = grey100())

        MarkdownToolbar()
    }
}

/**
 * 마크다운 도구 툴바. 현재는 버튼 배치만 구현된 상태 (기능 미연결).
 * 아이콘 24dp, 아이콘 간 간격 없음, 아이콘 집합과 빈 공간의 여백은 상하좌우 16dp
 */
@Composable
private fun MarkdownToolbar(
    onClickTextSize: () -> Unit = {},
    onClickImage: () -> Unit = {},
    onClickVote: () -> Unit = {},
    onClickLink: () -> Unit = {},
    onClickBold: () -> Unit = {},
    onClickItalic: () -> Unit = {},
    onClickUnderline: () -> Unit = {},
    onClickStrikethrough: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 텍스트 크기
        MarkdownToolbarIcon(iconRes = R.drawable.ic_text_size, onClick = onClickTextSize)

        // 이미지 첨부
        MarkdownToolbarIcon(iconRes = R.drawable.ic_camera, onClick = onClickImage)

        // 투표
        MarkdownToolbarIcon(iconRes = R.drawable.ic_vote, onClick = onClickVote)

        // 링크
        MarkdownToolbarIcon(iconRes = R.drawable.ic_blog_link, onClick = onClickLink)

        // 굵게
        MarkdownToolbarIcon(iconRes = R.drawable.ic_border, onClick = onClickBold)

        // 기울임
        MarkdownToolbarIcon(iconRes = R.drawable.ic_italic, onClick = onClickItalic)

        // 밑줄
        MarkdownToolbarIcon(iconRes = R.drawable.ic_underline, onClick = onClickUnderline)

        // 취소선
        MarkdownToolbarIcon(iconRes = R.drawable.ic_strikethrough, onClick = onClickStrikethrough)
    }
}

@Composable
private fun MarkdownToolbarIcon(
    iconRes: Int,
    onClick: () -> Unit = {},
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = grey600(),
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() },
    )
}

/** 게시판 분류 칩. 드롭다운 칩은 선택 시 선택된 이름으로 라벨이 바뀜 */
@Composable
private fun WriteBoardChip(
    text: String,
    isSelected: Boolean,
    hasDropdownIcon: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) grey950() else grey100())
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
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

/** 카테고리/지부/학교/파트 공용 선택 바텀시트 */
@Composable
private fun WriteSelectSheetContent(
    title: String,
    items: List<String>,
    onSelect: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .padding(vertical = 16.dp),
            ) {
                UText(
                    text = item,
                    style = UmcTypographyTokens.Body,
                    color = grey800(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeWriteScreenPreview() {
    NoticeWriteScreen(
        uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.CENTRAL_STAFF,
            selectedCategory = WriteCategory(
                WriteCategoryType.CENTRAL_STAFF,
                AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL
            ),
            boardChips = listOf(
                BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART, BoardChipType.CHAPTER
            ),
            boardHint = AppStrings.NOTICE_WRITE_CLASS_HINT,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoticeWriteScreenSuperAdminPreview() {
    NoticeWriteScreen(
        uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.SUPER_ADMIN,
        ),
    )
}
