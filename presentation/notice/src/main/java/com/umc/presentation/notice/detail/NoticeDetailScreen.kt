package com.umc.presentation.notice.detail

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green600
import com.umc.component.theme.green500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500
import com.umc.domain.model.enums.NoticeVoteStatus
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.notice.NoticeTags
import com.umc.presentation.notice.formatNoticeDate
import com.umc.presentation.notice.write.MarkdownRenderer
import com.umc.presentation.notice.write.drawMarkdownQuoteBars
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailRoute(
    noticeId: Long,
    viewModel: NoticeDetailViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToEdit: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showReadStatusSheet by remember { mutableStateOf(false) }
    var showVoteStatusScreen by remember { mutableStateOf(false) }
    val readStatusSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(noticeId) {
        viewModel.load(noticeId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                NoticeDetailEvent.MoveBack -> navigateToBack()
                is NoticeDetailEvent.MoveToEdit -> navigateToEdit(event.noticeId)
                is NoticeDetailEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 투표 현황은 전체 화면으로 전환
    if (showVoteStatusScreen) {
        BackHandler { showVoteStatusScreen = false }

        NoticeVoteStatusScreen(
            sections = uiState.voteParticipantSections,
            isLoading = uiState.isLoadingVoteParticipants,
            onClickBack = { showVoteStatusScreen = false },
        )
        return
    }

    NoticeDetailScreen(
        uiState = uiState,
        onClickBack = navigateToBack,
        onClickEdit = viewModel::onClickEdit,
        onClickDelete = viewModel::onClickDelete,
        onClickVoteOption = viewModel::onClickVoteOption,
        onClickVoteButton = viewModel::onClickVoteButton,
        onClickVoteStatus = {
            viewModel.loadVoteParticipants()
            showVoteStatusScreen = true
        },
        onClickReadStatus = { showReadStatusSheet = true },
    )

    if (showReadStatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReadStatusSheet = false },
            sheetState = readStatusSheetState,
            containerColor = grey000(),
        ) {
            NoticeReadStatusSheetContent(
                statistics = uiState.readStatistics,
                unreadList = uiState.unreadList,
                readList = uiState.readList,
                isReminderSent = uiState.isReminderSent,
                isSendingReminder = uiState.isSendingReminder,
                onLoadMore = viewModel::loadMoreReadStatus,
                onClickSendReminder = viewModel::onClickSendReminder,
            )
        }
    }
}

@Composable
fun NoticeDetailScreen(
    uiState: NoticeDetailUiState = NoticeDetailUiState(),
    onClickBack: () -> Unit = {},
    onClickEdit: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickVoteOption: (NoticeVoteOption) -> Unit = {},
    onClickVoteButton: () -> Unit = {},
    onClickVoteStatus: () -> Unit = {},
    onClickReadStatus: () -> Unit = {},
) {
    val detail = uiState.detail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        // 상단 바: 뒤로가기 + 타이틀 + (작성자 전용) 케밥 메뉴
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 10.dp, top = 8.dp),
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
                text = AppStrings.HOME_NOTICE_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.isAuthor) {
                NoticeDetailMenu(
                    onClickEdit = onClickEdit,
                    onClickDelete = onClickDelete,
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = indigo500(), trackColor = indigo100())
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 수신 대상 태그 (목록 카드와 동일)
            NoticeTags(target = detail.targetInfo)

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                text = detail.title,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 작성자
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileImage(url = uiState.authorProfileImageUrl, size = 20)

                Spacer(modifier = Modifier.width(8.dp))

                UText(
                    text = uiState.authorName,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey950(),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜 ｜ 조회수 ｜ 수신대상
            UText(
                text = listOf(
                    formatNoticeDate(detail.createdAt),
                    "${AppStrings.NOTICE_VIEW_COUNT} ${detail.viewCount}",
                    uiState.receiverText,
                )
                    .filter { it.isNotBlank() }
                    .joinToString("  |  "),
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = grey200())

            Spacer(modifier = Modifier.height(16.dp))

            // 본문 (작성 화면과 동일한 마크다운 렌더링)
            val markerColor = grey400()
            val linkColor = indigo500()
            val quoteBarColor = grey300()
            val markdownRenderer = remember(markerColor, linkColor) {
                MarkdownRenderer(markerColor = markerColor, linkColor = linkColor)
            }
            val rendered = remember(detail.content, markdownRenderer) {
                markdownRenderer.render(detail.content)
            }
            var bodyTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

            Text(
                text = rendered.transformed.text,
                onTextLayout = { bodyTextLayout = it },
                // 인용구 세로선은 좌측 여백(음수 x)에 그려 본문 위치를 그대로 둔다
                modifier = Modifier.drawBehind {
                    bodyTextLayout?.let { layout ->
                        drawMarkdownQuoteBars(layout, rendered.quoteBlocks, quoteBarColor)
                    }
                },
                style = UmcTypographyTokens.Subheadline.copy(
                    color = grey950(),
                    lineHeight = TextUnit.Unspecified,
                ),
            )

            // 첨부 이미지
            val isInspectionMode = LocalInspectionMode.current
            detail.images.sortedBy { it.displayOrder }.forEach { image ->
                Spacer(modifier = Modifier.height(16.dp))

                if (isInspectionMode) {
                    // 프리뷰에서는 coil을 로드하지 않으므로 자리 표시자로 대체
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(grey100()),
                    )
                } else {
                    AsyncImage(
                        model = image.url,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(grey100()),
                    )
                }
            }

            // 투표
            detail.vote?.let { vote ->
                Spacer(modifier = Modifier.height(32.dp))

                NoticeDetailVoteSection(
                    vote = vote,
                    selectedOptionIds = uiState.selectedOptionIds,
                    showResult = uiState.showVoteResult,
                    voteStatus = uiState.voteStatus,
                    onClickOption = onClickVoteOption,
                    onClickButton = onClickVoteButton,
                    onClickVoteStatus = onClickVoteStatus,
                )
            }

            // 관련 링크
            detail.links.sortedBy { it.displayOrder }.forEach { link ->
                Spacer(modifier = Modifier.height(24.dp))

                NoticeDetailLinkCard(url = link.url)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // 수신 확인 현황 (작성자 전용, 하단 고정)
        if (uiState.isAuthor) {
            NoticeReadStatusBar(
                statistics = uiState.readStatistics,
                onClick = onClickReadStatus,
            )
        }
    }
}

/** 작성자 전용 케밥 메뉴 (수정하기/삭제하기) */
@Composable
private fun NoticeDetailMenu(
    onClickEdit: () -> Unit = {},
    onClickDelete: () -> Unit = {},
) {
    var isMenuVisible by remember { mutableStateOf(false) }

    Box {
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .clickable { isMenuVisible = true },
            painter = painterResource(id = R.drawable.ic_menu_kebab),
            contentDescription = null,
            tint = grey950(),
        )

        DropdownMenu(
            expanded = isMenuVisible,
            onDismissRequest = { isMenuVisible = false },
            containerColor = grey000(),
            shape = RoundedCornerShape(16.dp),
        ) {
            NoticeDetailMenuItem(
                label = AppStrings.NOTICE_DETAIL_MENU_EDIT,
                iconRes = R.drawable.ic_edit,
                color = grey950(),
            ) {
                isMenuVisible = false
                onClickEdit()
            }

            NoticeDetailMenuItem(
                label = AppStrings.NOTICE_DETAIL_MENU_DELETE,
                iconRes = R.drawable.ic_trash_can,
                color = red500(),
            ) {
                isMenuVisible = false
                onClickDelete()
            }
        }
    }
}

@Composable
private fun NoticeDetailMenuItem(
    label: String,
    iconRes: Int,
    color: Color,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        UText(
            text = label,
            style = UmcTypographyTokens.Subheadline,
            color = color,
            modifier = Modifier.weight(1f),
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = color,
        )
    }
}

/** 관련 링크 카드 (연회색). 클릭 시 브라우저로 이동 */
@Composable
private fun NoticeDetailLinkCard(url: String) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(grey100())
            .clickable {
                val uri = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                runCatching { uriHandler.openUri(uri) }
            }
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_blog_link),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(24.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UText(
                text = AppStrings.NOTICE_DETAIL_ABOUT_LINK_TITLE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey900(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            UText(
                text = url,
                style = UmcTypographyTokens.Subheadline,
                color = grey500(),
                maxLines = 1,
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = grey400(),
        )
    }
}

/** 하단 고정 수신 확인 현황 바 (작성자 전용). 탭하면 열람 현황 시트 */
@Composable
private fun NoticeReadStatusBar(
    statistics: NoticeReadStatistics?,
    onClick: () -> Unit = {},
) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = grey200())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_success),
                    contentDescription = null,
                    tint = green500(),
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                UText(
                    text = AppStrings.NOTICE_DETAIL_CONFIRMED_PEOPLE_CHECK,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                    modifier = Modifier.weight(1f),
                )

                val readCount = statistics?.readCount ?: 0
                val totalCount = statistics?.totalCount ?: 0
                val percent = if (totalCount > 0) readCount * 100 / totalCount else 0

                UText(
                    text = "$readCount",
                    style = UmcTypographyTokens.BodyBold,
                    color = green600(),
                )

                UText(
                    text = "/${totalCount}명 (${percent}%)",
                    style = UmcTypographyTokens.Body,
                    color = grey950(),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val readRate = statistics
                ?.takeIf { it.totalCount > 0 }
                ?.let { it.readCount.toFloat() / it.totalCount }
                ?: 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(grey200()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(readRate)
                        .clip(CircleShape)
                        .background(green500()),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            UText(
                text = AppStrings.NOTICE_DETAIL_READ_MANAGE,
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeDetailScreenPreview() {
    NoticeDetailScreen(
        uiState = NoticeDetailUiState(
            detail = NoticeDetail(
                id = 1L,
                title = "[필독] 12기 활동 규정 안내",
                content = "안녕하세요. 운영진입니다.\n[필독] 12기 활동 규정 안내에 대해 안내드립니다.\n\n자세한 내용은 첨부파일을 확인해주세요.\n문의사항은 운영진에게 연락 바랍니다.\n감사합니다.",
                targetInfo = NoticeTarget(targetGisuId = 1),
                viewCount = 100,
                createdAt = "2026-01-01T00:00:00",
                vote = NoticeVote(
                    voteId = 1L,
                    title = "회식 메뉴 투표",
                    options = listOf(
                        NoticeVoteOption(optionId = 1L, content = "삼겹살 목살"),
                        NoticeVoteOption(optionId = 2L, content = "치킨"),
                        NoticeVoteOption(optionId = 3L, content = "피자"),
                    ),
                ),
            ),
            isLoading = false,
            isAuthor = true,
            authorName = "운영진",
            receiverText = "수신대상: 12기/전체",
            voteStatus = NoticeVoteStatus.OPEN,
            readStatistics = NoticeReadStatistics(totalCount = 1000, readCount = 801, unreadCount = 199, readRate = 80.0),
        ),
    )
}
