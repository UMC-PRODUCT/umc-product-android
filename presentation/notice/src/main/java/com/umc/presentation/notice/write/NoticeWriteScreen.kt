package com.umc.presentation.notice.write

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.umc.component.base.CollectUiEvents
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextActionItem
import com.umc.component.component.uTextActionMenu
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.NoticeImageAttachment
import com.umc.domain.model.notice.WriteCategory
import kotlinx.coroutines.flow.collectLatest

/** 공지 작성 화면 바텀시트 종류 */
private enum class WriteSheetType { CATEGORY, CHAPTER, SCHOOL, PART, VOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeWriteRoute(
    editNoticeId: Long = 0L,
    viewModel: NoticeWriteViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    /** 등록·수정 성공 시. 목록 새로고침 후 뒤로 가기 위해 분리 */
    onSubmitSuccess: () -> Unit = navigateToBack,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var sheetType by remember { mutableStateOf<WriteSheetType?>(null) }
    var showVoteMaxDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(NoticeWriteViewModel.MAX_IMAGE_COUNT)
    ) { uris ->
        viewModel.onAddImages(uris)
    }

    LaunchedEffect(editNoticeId) {
        if (editNoticeId > 0L) viewModel.initEditMode(editNoticeId)
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
            when (event) {
                NoticeWriteEvent.SubmitSuccess -> {
                    val message = if (editNoticeId > 0L) {
                        AppStrings.NOTICE_WRITE_EDIT_SUCCESS
                    } else {
                        AppStrings.NOTICE_WRITE_SUCCESS
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    onSubmitSuccess()
                }

                is NoticeWriteEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

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
        onSelectHeading = viewModel::onSelectHeading,
        onClickImage = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        // 투표는 최대 1개: 이미 있으면 안내 다이얼로그
        onClickVote = {
            if (uiState.vote == null) {
                sheetType = WriteSheetType.VOTE
            } else {
                showVoteMaxDialog = true
            }
        },
        onClickModifyVote = { sheetType = WriteSheetType.VOTE },
        onClickLink = viewModel::onShowLinkPanel,
        onClickBold = viewModel::onClickBold,
        onClickItalic = viewModel::onClickItalic,
        onClickUnderline = viewModel::onClickUnderline,
        onClickStrikethrough = viewModel::onClickStrikethrough,
        onSelectHighlight = viewModel::onSelectHighlight,
        onClickBullet = viewModel::onClickBullet,
        onClickQuote = viewModel::onClickQuote,
        onRemoveImage = viewModel::onRemoveImage,
        onLinkTextChanged = viewModel::onLinkTextChanged,
        onCloseLinkPanel = viewModel::onHideLinkPanel,
        onClickAiRefine = viewModel::onClickAiRefine,
        onClickAiSummarize = { viewModel.onClickAiSummarize(readClipboardText(context)) },
    )

    if (showVoteMaxDialog) {
        VoteMaxDialog(onDismiss = { showVoteMaxDialog = false })
    }

    if (uiState.isUploadingImages) {
        ProcessingDialog(message = AppStrings.NOTICE_WRITE_IMAGE_UPLOADING)
    }

    if (uiState.isAiProcessing) {
        // 모델 다운로드 중에는 진행률을, 추론 중에는 처리 문구를 보여줌
        ProcessingDialog(
            message = uiState.aiDownloadPercent
                ?.let { AppStrings.AI_MODEL_DOWNLOADING.format(it) }
                ?: AppStrings.AI_PROCESSING,
        )
    }

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

                WriteSheetType.VOTE -> NoticeVoteSheetContent(
                    existingVote = uiState.vote,
                    onComplete = { vote ->
                        viewModel.onAttachVote(vote)
                        sheetType = null
                    },
                    onDelete = {
                        viewModel.onDeleteVote()
                        sheetType = null
                    },
                )
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
    onContentChanged: (TextFieldValue) -> Unit = {},
    onSelectHeading: (MarkdownHeading) -> Unit = {},
    onClickImage: () -> Unit = {},
    onClickVote: () -> Unit = {},
    onClickModifyVote: () -> Unit = {},
    onClickLink: () -> Unit = {},
    onClickBold: () -> Unit = {},
    onClickItalic: () -> Unit = {},
    onClickUnderline: () -> Unit = {},
    onClickStrikethrough: () -> Unit = {},
    onSelectHighlight: (MarkdownHighlightColor) -> Unit = {},
    onClickBullet: () -> Unit = {},
    onClickQuote: () -> Unit = {},
    onClickAiSummarize: () -> Unit = {},
    onRemoveImage: (NoticeImageAttachment) -> Unit = {},
    onLinkTextChanged: (String) -> Unit = {},
    onCloseLinkPanel: () -> Unit = {},
    onClickAiRefine: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
            .imePadding(),
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

            // 수정 모드에서는 수신 대상(카테고리/게시판 분류) 변경 불가
            if (!uiState.isEditMode) {
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

            // 내용 입력. 마크다운 원문을 그대로 편집하고 스타일만 입혀서 보여줌
            val markerColor = grey400()
            val linkColor = indigo500()
            val quoteBarColor = grey300()
            // 편집기와 인용구 세로선이 같은 파싱 결과를 공유하도록 렌더러를 하나만 둔다
            val markdownRenderer = remember(markerColor, linkColor) {
                MarkdownRenderer(markerColor = markerColor, linkColor = linkColor)
            }
            val rendered = markdownRenderer.render(uiState.content.text)
            var contentTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

            // 지원 기기에서만 AI 항목을 메뉴에 추가
            val aiActions = if (uiState.isAiRefineEnabled) {
                listOf(UTextActionItem(AppStrings.AI_MENU_REFINE, onClickAiRefine))
            } else {
                emptyList()
            }

            BasicTextField(
                    value = uiState.content,
                    onValueChange = onContentChanged,
                    // 제목 줄(28sp 등)이 본문 lineHeight(20sp)에 잘리지 않도록 줄 높이는 폰트 크기를 따르게 함
                    textStyle = UmcTypographyTokens.Subheadline.copy(
                        color = grey950(),
                        lineHeight = TextUnit.Unspecified,
                    ),
                    cursorBrush = SolidColor(grey950()),
                    visualTransformation = markdownRenderer.visualTransformation,
                    onTextLayout = { contentTextLayout = it },
                    // 내부 스크롤이 생기면 세로선 좌표가 어긋나므로 높이를 고정하지 않는다
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .uTextActionMenu(aiActions),
                    decorationBox = { innerTextField ->
                        // 세로선은 좌측 여백(음수 x)에 그려 본문 위치를 그대로 둔다
                        Box(
                            modifier = Modifier.drawBehind {
                                contentTextLayout?.let { layout ->
                                    drawMarkdownQuoteBars(layout, rendered.quoteBlocks, quoteBarColor)
                                }
                            }
                        ) {
                            if (uiState.content.text.isEmpty()) {
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

            // 첨부 이미지 (정사각 썸네일 가로 나열)
            if (uiState.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.images.forEach { image ->
                        AttachedImageThumbnail(
                            image = image,
                            onRemove = { onRemoveImage(image) },
                        )
                    }
                }
            }

            // 링크 첨부 패널
            if (uiState.isLinkVisible) {
                Spacer(modifier = Modifier.height(16.dp))

                NoticeLinkPanel(
                    linkText = uiState.linkText,
                    onLinkTextChanged = onLinkTextChanged,
                    onClose = onCloseLinkPanel,
                )
            }

            // 첨부 투표 요약 행
            uiState.vote?.let { vote ->
                Spacer(modifier = Modifier.height(16.dp))

                NoticeVoteRow(vote = vote, onClickModify = onClickModifyVote)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        HorizontalDivider(thickness = 1.dp, color = grey200())

        MarkdownToolbar(
            onSelectHeading = onSelectHeading,
            onClickImage = onClickImage,
            onClickVote = onClickVote,
            onClickLink = onClickLink,
            onClickBold = onClickBold,
            onClickItalic = onClickItalic,
            onClickUnderline = onClickUnderline,
            onClickStrikethrough = onClickStrikethrough,
            onSelectHighlight = onSelectHighlight,
            highlightColor = uiState.highlightColor,
            onClickBullet = onClickBullet,
            onClickQuote = onClickQuote,
            // 커서 위치에서 켜져 있는 마크다운을 툴바에 표시하기 위해 매 입력마다 다시 계산한다
            activeStyles = remember(uiState.content) {
                MarkdownScanner.activeStyles(uiState.content)
            },
            isAiEnabled = uiState.isAiRefineEnabled,
            onClickAiRefine = onClickAiRefine,
            onClickAiSummarize = onClickAiSummarize,
        )
    }
}

/**
 * 마크다운 도구 툴바.
 * 아이콘 24dp, 상하좌우 16dp 여백. 아이콘 수가 화면 폭을 넘으므로 가로 스크롤한다
 */
@Composable
private fun MarkdownToolbar(
    onSelectHeading: (MarkdownHeading) -> Unit = {},
    onClickImage: () -> Unit = {},
    onClickVote: () -> Unit = {},
    onClickLink: () -> Unit = {},
    onClickBold: () -> Unit = {},
    onClickItalic: () -> Unit = {},
    onClickUnderline: () -> Unit = {},
    onClickStrikethrough: () -> Unit = {},
    onSelectHighlight: (MarkdownHighlightColor) -> Unit = {},
    highlightColor: MarkdownHighlightColor? = null,
    onClickBullet: () -> Unit = {},
    onClickQuote: () -> Unit = {},
    activeStyles: MarkdownActiveStyles = MarkdownActiveStyles(),
    isAiEnabled: Boolean = false,
    onClickAiRefine: () -> Unit = {},
    onClickAiSummarize: () -> Unit = {},
) {
    var showTextSizeMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }
    var showAiMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MARKDOWN_TOOLBAR_ICON_GAP),
    ) {
        // AI (미지원 기기에서는 노출하지 않는다)
        if (isAiEnabled) {
            Box {
                MarkdownToolbarIcon(iconRes = R.drawable.ic_ai, onClick = { showAiMenu = true })

                MarkdownMenu(expanded = showAiMenu, onDismissRequest = { showAiMenu = false }) {
                    MarkdownMenuItem(label = AppStrings.AI_MENU_REFINE) {
                        showAiMenu = false
                        onClickAiRefine()
                    }
                    MarkdownMenuItem(label = AppStrings.AI_MENU_SUMMARIZE) {
                        showAiMenu = false
                        onClickAiSummarize()
                    }
                }
            }
        }

        // 텍스트 크기 (제목1/제목2/제목3/본문 메뉴)
        Box {
            MarkdownToolbarIcon(
                iconRes = R.drawable.ic_text_size,
                isActive = activeStyles.heading != MarkdownHeading.BODY,
                onClick = { showTextSizeMenu = true },
            )

            MarkdownMenu(
                expanded = showTextSizeMenu,
                onDismissRequest = { showTextSizeMenu = false },
            ) {
                TextSizeMenuItem(
                    label = MarkdownHeading.TITLE1.label,
                    style = UmcTypographyTokens.Title2Bold,
                ) {
                    onSelectHeading(MarkdownHeading.TITLE1)
                    showTextSizeMenu = false
                }

                TextSizeMenuItem(
                    label = MarkdownHeading.TITLE2.label,
                    style = UmcTypographyTokens.Title3Bold,
                ) {
                    onSelectHeading(MarkdownHeading.TITLE2)
                    showTextSizeMenu = false
                }

                TextSizeMenuItem(
                    label = MarkdownHeading.TITLE3.label,
                    style = UmcTypographyTokens.HeadlineBold,
                ) {
                    onSelectHeading(MarkdownHeading.TITLE3)
                    showTextSizeMenu = false
                }

                TextSizeMenuItem(
                    label = MarkdownHeading.BODY.label,
                    style = UmcTypographyTokens.Subheadline,
                ) {
                    onSelectHeading(MarkdownHeading.BODY)
                    showTextSizeMenu = false
                }
            }
        }

        // 이미지 첨부
        MarkdownToolbarIcon(iconRes = R.drawable.ic_camera, onClick = onClickImage)

        // 투표
        MarkdownToolbarIcon(iconRes = R.drawable.ic_vote, onClick = onClickVote)

        // 링크
        MarkdownToolbarIcon(iconRes = R.drawable.ic_blog_link, onClick = onClickLink)

        // 굵게
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_border,
            isActive = MarkdownStyle.BOLD in activeStyles,
            onClick = onClickBold,
        )

        // 기울임
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_italic,
            isActive = MarkdownStyle.ITALIC in activeStyles,
            onClick = onClickItalic,
        )

        // 밑줄
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_underline,
            isActive = MarkdownStyle.UNDERLINE in activeStyles,
            onClick = onClickUnderline,
        )

        // 취소선
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_strikethrough,
            isActive = MarkdownStyle.STRIKETHROUGH in activeStyles,
            onClick = onClickStrikethrough,
        )

        // 형광펜 (색상 선택 메뉴)
        Box {
            MarkdownToolbarIcon(
                iconRes = R.drawable.ic_highlighter,
                isActive = MarkdownStyle.HIGHLIGHT in activeStyles,
                onClick = { showHighlightMenu = true },
            )

            MarkdownMenu(
                expanded = showHighlightMenu,
                onDismissRequest = { showHighlightMenu = false },
                modifier = Modifier.heightIn(max = HIGHLIGHT_MENU_MAX_HEIGHT),
            ) {
                MarkdownHighlightColor.entries.forEach { color ->
                    MarkdownMenuItem(
                        label = color.label,
                        swatch = color.swatch,
                        isSelected = color == highlightColor,
                    ) {
                        showHighlightMenu = false
                        onSelectHighlight(color)
                    }
                }
            }
        }

        // 글머리 기호
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_bullet_list,
            isActive = activeStyles.isBullet,
            onClick = onClickBullet,
        )

        // 인용구
        MarkdownToolbarIcon(
            iconRes = R.drawable.ic_quote,
            isActive = activeStyles.isQuote,
            onClick = onClickQuote,
        )
    }
}

/** 툴바 아이콘 간격. 기존 균등 분배(SpaceBetween) 때의 간격을 유지 */
private val MARKDOWN_TOOLBAR_ICON_GAP = 20.dp

/** 형광펜 메뉴는 4행까지만 보이고 나머지는 스크롤 (디자인 기준 192dp) */
private val HIGHLIGHT_MENU_MAX_HEIGHT = 192.dp

/**
 * 아이콘 위쪽으로 메뉴를 띄울 거리.
 * 아이콘 위에는 툴바 여백 16dp가 있으므로, 그 위로 8dp 더 올려 툴바에 바로 붙인다
 */
private val MARKDOWN_MENU_LIFT = 24.dp

/** 메뉴가 화면 밖으로 나가지 않도록 두는 좌우 여백 */
private val MARKDOWN_MENU_SCREEN_MARGIN = 16.dp

/**
 * 툴바 아이콘에서 펼쳐지는 메뉴 카드.
 *
 * Material3 DropdownMenu는 시스템 바 높이만큼 위로 밀려 툴바와 멀어지므로
 * 위치를 직접 계산한다. 가로는 누른 아이콘에 맞추고, 세로는 툴바 바로 위에 붙인다
 */
@Composable
private fun MarkdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return

    val density = LocalDensity.current
    val positionProvider = remember(density) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val margin = with(density) { MARKDOWN_MENU_SCREEN_MARGIN.roundToPx() }
                val lift = with(density) { MARKDOWN_MENU_LIFT.roundToPx() }
                val maxX = (windowSize.width - popupContentSize.width - margin)
                    .coerceAtLeast(margin)
                return IntOffset(
                    x = anchorBounds.left.coerceIn(margin, maxX),
                    y = (anchorBounds.top - popupContentSize.height - lift).coerceAtLeast(margin),
                )
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = grey000(),
            shadowElevation = 8.dp,
        ) {
            // Popup 안에서는 최대 폭이 화면 전체라 항목의 fillMaxWidth가 화면을 다 채운다.
            // 가장 넓은 항목 기준으로 카드 폭을 잡는다
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .verticalScroll(rememberScrollState()),
                content = content,
            )
        }
    }
}

/**
 * 메뉴 항목. [swatch]를 주면 앞에 색상 원이 붙고,
 * 선택된 항목은 원 안에 체크가 들어간다 (형광펜 메뉴)
 */
@Composable
private fun MarkdownMenuItem(
    label: String,
    swatch: Color? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (swatch != null) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(swatch),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = grey000(),
                        modifier = Modifier.size(11.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
        }

        UText(
            text = label,
            style = UmcTypographyTokens.Subheadline,
            color = grey950(),
        )
    }
}

/** 텍스트 크기 메뉴 항목. 각 항목은 적용될 크기감으로 표시 */
@Composable
private fun TextSizeMenuItem(
    label: String,
    style: TextStyle,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        UText(
            text = label,
            style = style,
            color = grey950(),
        )
    }
}

@Composable
private fun MarkdownToolbarIcon(
    iconRes: Int,
    isActive: Boolean = false,
    onClick: () -> Unit = {},
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = if (isActive) indigo500() else grey600(),
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() },
    )
}

/** 본문 아래에 표시되는 첨부 이미지 정사각 썸네일. 우상단 버튼으로 삭제 */
@Composable
private fun AttachedImageThumbnail(
    image: NoticeImageAttachment,
    onRemove: () -> Unit = {},
) {
    Box(modifier = Modifier.size(96.dp)) {
        val thumbnailModifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, grey200(), RoundedCornerShape(12.dp))

        // 프리뷰에서는 coil을 로드하지 않으므로 자리 표시자로 대체
        if (LocalInspectionMode.current) {
            Box(modifier = thumbnailModifier.background(grey100()))
        } else {
            AsyncImage(
                model = image.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = thumbnailModifier,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(grey400())
                .clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_big),
                contentDescription = null,
                tint = grey000(),
                modifier = Modifier.size(10.dp),
            )
        }
    }
}

/** 본문 아래에 표시되는 링크 첨부 패널. 우상단 X로 닫음 */
@Composable
private fun NoticeLinkPanel(
    linkText: String,
    onLinkTextChanged: (String) -> Unit = {},
    onClose: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(grey100())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_blog_link),
                contentDescription = null,
                tint = indigo500(),
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = AppStrings.NOTICE_WRITE_LINK_TITLE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_close_big),
                contentDescription = null,
                tint = grey500(),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClose() },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(grey000())
                .border(1.dp, grey200(), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 15.dp),
        ) {
            BasicTextField(
                value = linkText,
                onValueChange = onLinkTextChanged,
                textStyle = UmcTypographyTokens.Subheadline.copy(color = grey950()),
                cursorBrush = SolidColor(grey950()),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (linkText.isEmpty()) {
                            UText(
                                text = AppStrings.NOTICE_WRITE_LINK_HINT,
                                style = UmcTypographyTokens.Subheadline,
                                color = grey400(),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

/** 투표는 공지당 최대 1개임을 안내하는 다이얼로그 */
@Composable
private fun VoteMaxDialog(
    onDismiss: () -> Unit = {},
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = grey000(),
        shape = RoundedCornerShape(20.dp),
        text = {
            UText(
                text = AppStrings.NOTICE_WRITE_VOTE_MAX,
                style = UmcTypographyTokens.Title3Bold,
                color = grey950(),
            )
        },
        confirmButton = {
            UButton(
                text = AppStrings.CONFIRM,
                onClick = onDismiss,
                backgroundColor = grey100(),
                textColor = grey800(),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

/** 진행 다이얼로그 (이미지 업로드 / AI 처리 공용) */
@Composable
private fun ProcessingDialog(message: String) {
    Dialog(onDismissRequest = { }) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(grey000())
                .padding(horizontal = 48.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                color = indigo500(),
                trackColor = indigo100(),
                strokeWidth = 5.dp,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            UText(
                text = message,
                style = UmcTypographyTokens.Subheadline,
                color = grey700(),
            )
        }
    }
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

/**
 * 클립보드의 텍스트를 읽는다. 여러 항목이면 줄바꿈으로 잇는다.
 * Android 10부터 포커스가 있는 앱만 읽을 수 있는데, 메뉴를 눌러 호출되므로 문제없다
 */
private fun readClipboardText(context: Context): String {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return ""
    val clip = manager.primaryClip ?: return ""
    return (0 until clip.itemCount)
        .mapNotNull { clip.getItemAt(it)?.coerceToText(context)?.toString() }
        .joinToString("\n")
        .trim()
}
