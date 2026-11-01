package com.example.mypage.memberprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.activityhistory.ActiveHistorySection
import com.umc.component.R
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey50
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main500
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.domain.model.ProfileInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.UserActiveItem
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun MemberProfileRoute(
    viewModel: MemberProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var toastData by remember { mutableStateOf<UToastData?>(null) }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is MemberProfileEvent.ShowToast -> toastData = UToastData(
                message = event.message,
                state = if (event.isSuccess) UToastState.CHECK else UToastState.ERROR
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MemberProfileScreen(
            uiState = uiState,
            onBackClick = onNavigateBack,
            onMenuClick = viewModel::onClickMenu,
            onDismissMenu = viewModel::onDismissMenu,
            onBlockMenuClick = viewModel::onClickBlockMenu,
            onReportMenuClick = viewModel::onClickReportMenu,
            onRetryClick = viewModel::onClickRetry,
        )

        UToastHost(
            data = toastData,
            onDismiss = { toastData = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }

    if (uiState.isReportSheetOpen) {
        MemberReportBottomSheet(
            selectedReasons = uiState.reportReasons,
            detail = uiState.reportDetail,
            canSubmit = uiState.canSubmitReport,
            onToggleReason = viewModel::onToggleReportReason,
            onDetailChange = viewModel::onReportDetailChange,
            onCancel = viewModel::onDismissReportSheet,
            onSubmit = viewModel::onClickReportSubmit,
        )
    }

    when (uiState.dialog) {
        MemberProfileDialog.BLOCK -> MemberProfileConfirmDialog(
            title = AppStrings.MEMBER_BLOCK_CONFIRM_TITLE,
            description = AppStrings.MEMBER_BLOCK_CONFIRM_CONTENT,
            positiveText = AppStrings.MEMBER_BLOCK_CONFIRM,
            onConfirm = viewModel::onConfirmBlock,
            onDismiss = viewModel::onDismissDialog,
        )

        MemberProfileDialog.UNBLOCK -> MemberProfileConfirmDialog(
            title = AppStrings.MEMBER_UNBLOCK_CONFIRM_TITLE,
            description = AppStrings.MEMBER_UNBLOCK_CONFIRM_CONTENT,
            positiveText = AppStrings.MEMBER_UNBLOCK_CONFIRM,
            onConfirm = viewModel::onConfirmBlock,
            onDismiss = viewModel::onDismissDialog,
        )

        MemberProfileDialog.REPORT -> MemberProfileConfirmDialog(
            title = AppStrings.MEMBER_REPORT_CONFIRM_TITLE,
            description = AppStrings.MEMBER_REPORT_CONFIRM_CONTENT,
            positiveText = AppStrings.MEMBER_REPORT_SUBMIT,
            onConfirm = viewModel::onConfirmReport,
            onDismiss = viewModel::onDismissDialog,
        )

        null -> Unit
    }
}

/**
 * 차단·차단 해제·신고 확인 창: 제목 + 본문 + 버튼 2개 (아이콘 없음, 시안 04·05·06)
 *
 * 스레드의 메시지 신고 확인 창과 같은 compact 형태를 쓴다
 */
@Composable
private fun MemberProfileConfirmDialog(
    title: String,
    description: String,
    positiveText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    UDialog(
        title = title,
        subtitle = description,
        isTwoButton = true,
        useCompactStyle = true,
        positiveText = positiveText,
        negativeBackgroundColor = grey100(),
        positiveBackgroundColor = red100(),
        positiveTextColor = red500(),
        onPositive = onConfirm,
        onNegative = onDismiss,
        onDismissRequest = onDismiss,
    )
}

/**
 * 스레드 > 프로필 정보 보기 화면 (타인 프로필, 수정 불가)
 */
@Composable
fun MemberProfileScreen(
    uiState: MemberProfileUiState,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onBlockMenuClick: () -> Unit,
    onReportMenuClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50())
    ) {
        MemberProfileTopBar(
            showMenu = !uiState.isMine && !uiState.isLoadFailed && !uiState.isLoading,
            isMenuOpen = uiState.isMenuOpen,
            isBlocked = uiState.isBlocked,
            isBlockMenuEnabled = uiState.isBlockStateKnown,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onDismissMenu = onDismissMenu,
            onBlockMenuClick = onBlockMenuClick,
            onReportMenuClick = onReportMenuClick,
        )

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = main500())
            }

            uiState.isLoadFailed -> MemberProfileLoadError(onRetryClick = onRetryClick)

            else -> MemberProfileContent(uiState = uiState)
        }
    }
}

@Composable
private fun MemberProfileContent(uiState: MemberProfileUiState) {
    val userInfo = uiState.userInfo
    val profile = userInfo.profile

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        //프로필 이미지 (이름/닉네임과 40dp 간격: 항목 간격 24dp + 16dp)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = userInfo.profileImageLink.ifBlank { R.drawable.ic_profile_default },
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(1.dp, grey200(), CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile_default),
                    error = painterResource(id = R.drawable.ic_profile_default)
                )
            }
        }

        //이름/닉네임
        item {
            MemberProfileField(
                title = AppStrings.MEMBER_PROFILE_NAME_NICKNAME,
                value = "${userInfo.name}/${userInfo.nickname}"
            )
        }

        //학교 + 기수/파트
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MemberProfileField(
                    title = AppStrings.PROFILE_SCHOOL,
                    value = userInfo.schoolName,
                    modifier = Modifier.weight(1f)
                )
                MemberProfileField(
                    title = AppStrings.PROFILE_PART,
                    value = uiState.gisuPartText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        //활동 이력 (마이페이지 > 활동 이력과 같은 컴포넌트)
        item {
            ActiveHistorySection(
                histories = uiState.histories,
                isLoading = uiState.isHistoryLoading
            )
        }

        //이메일·링크
        item {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // 회원 조회(MEMBER-101)는 비공개 링크를 null 로 내려줘 미입력과 구분되지 않고, 연락처 이메일은 내려주지 않는다.
                // 우선 값이 없으면 모두 비공개 항목으로 안내한다.
                // 응답의 email 은 연락처가 아니라 로그인 이메일이라, 공개 설정과 상관없이 드러나지 않게 쓰지 않는다
                // TODO 서버가 공개 여부나 연락처를 함께 내려주면 미입력과 비공개를 나눠 보여준다
                MemberProfileField(AppStrings.PROFILE_LINK_EMAIL, AppStrings.MEMBER_PROFILE_PRIVATE)
                MemberProfileField(AppStrings.PROFILE_LINK_GITHUB, profile.github.orPrivateLabel())
                MemberProfileField(AppStrings.PROFILE_LINK_LINKEDIN, profile.linkedIn.orPrivateLabel())
                MemberProfileField(AppStrings.PROFILE_LINK_BLOG, profile.blog.orPrivateLabel())
                MemberProfileField(AppStrings.PROFILE_LINK_INSTAGRAM, profile.instagram.orPrivateLabel())
                MemberProfileField(AppStrings.PROFILE_LINK_PERSONAL, profile.personal.orPrivateLabel())
            }
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

/**
 * 상단 바: 뒤로 가기 + 프로필 + 우측 더보기(사용자 차단·신고) 메뉴
 */
@Composable
private fun MemberProfileTopBar(
    showMenu: Boolean,
    isMenuOpen: Boolean,
    isBlocked: Boolean,
    isBlockMenuEnabled: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onBlockMenuClick: () -> Unit,
    onReportMenuClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey50())
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .clickable { onBackClick() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "뒤로가기",
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.MEMBER_PROFILE_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .weight(1f)
        )

        if (showMenu) {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_kebab),
                        contentDescription = AppStrings.MEMBER_PROFILE_MENU,
                        tint = grey800()
                    )
                }
                if (isMenuOpen) {
                    MemberProfileMenu(
                        isBlocked = isBlocked,
                        isBlockMenuEnabled = isBlockMenuEnabled,
                        onDismiss = onDismissMenu,
                        onBlockClick = onBlockMenuClick,
                        onReportClick = onReportMenuClick,
                    )
                }
            }
        }
    }
}

/**
 * 더보기 메뉴: 폭 180dp, 화면 오른쪽 끝에서 24dp, 더보기 버튼 바로 아래 (시안 01)
 *
 * M3 DropdownMenu 는 위아래 8dp 여백이 고정이라 시안(2dp)과 달라 Popup 으로 직접 그린다
 */
@Composable
private fun MemberProfileMenu(
    isBlocked: Boolean,
    isBlockMenuEnabled: Boolean,
    onDismiss: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit,
) {
    val endMarginPx = with(LocalDensity.current) { 24.dp.roundToPx() }
    val positionProvider = remember(endMarginPx) { MemberProfileMenuPositionProvider(endMarginPx) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        MemberProfileMenuContent(
            isBlocked = isBlocked,
            isBlockMenuEnabled = isBlockMenuEnabled,
            onBlockClick = onBlockClick,
            onReportClick = onReportClick,
        )
    }
}

@Composable
private fun MemberProfileMenuContent(
    isBlocked: Boolean,
    isBlockMenuEnabled: Boolean,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = white(),
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            // 차단 여부를 확인하기 전에는 누를 수 없게 흐리게 둔다
            MemberProfileMenuItem(
                text = if (isBlocked) AppStrings.MEMBER_PROFILE_MENU_UNBLOCK else AppStrings.MEMBER_PROFILE_MENU_BLOCK,
                color = if (isBlockMenuEnabled) grey950() else grey400(),
                enabled = isBlockMenuEnabled,
                onClick = onBlockClick,
            )
            MemberProfileMenuItem(
                text = AppStrings.MEMBER_PROFILE_MENU_REPORT,
                color = red500(),
                onClick = onReportClick,
            )
        }
    }
}

@Composable
private fun MemberProfileMenuItem(
    text: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        UText(text = text, style = UmcTypographyTokens.Subheadline, color = color)
    }
}

/** 메뉴를 앵커(더보기 버튼) 바로 아래, 화면 끝에서 [endMarginPx] 떨어진 곳에 둔다. 아래 공간이 없으면 위로 연다 */
private class MemberProfileMenuPositionProvider(private val endMarginPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            windowSize.width - popupContentSize.width - endMarginPx
        } else {
            endMarginPx
        }.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = if (anchorBounds.bottom + popupContentSize.height <= windowSize.height) {
            anchorBounds.bottom
        } else {
            (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
        }
        return IntOffset(x, y)
    }
}

/** 서버가 값을 내려주지 않은(null) 항목은 비공개 안내 문구로 바꾼다 */
private fun String.orPrivateLabel(): String = ifBlank { AppStrings.MEMBER_PROFILE_PRIVATE }

/**
 * 수정할 수 없는 타인 프로필 항목: 제목 + 회색 칸
 */
@Composable
private fun MemberProfileField(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        UText(text = title, style = UmcTypographyTokens.HeadlineBold, color = grey950())
        UText(
            text = value,
            style = UmcTypographyTokens.Callout,
            color = grey600(),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(grey100(), RoundedCornerShape(8.dp))
                .border(1.dp, grey200(), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

/**
 * 프로필 조회 실패 안내 + 다시 시도
 */
@Composable
private fun MemberProfileLoadError(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UText(
            text = AppStrings.PROFILE_LOAD_ERROR_TITLE,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        UText(
            text = AppStrings.PROFILE_LOAD_ERROR_DESCRIPTION,
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        UButton(
            text = AppStrings.PROFILE_RETRY,
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            backgroundColor = main500(),
            textColor = grey50(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            cornerRadius = 12.dp,
        )
    }
}

/**
 * 사용자 신고 사유 선택 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberReportBottomSheet(
    selectedReasons: ImmutableSet<MemberReportReason>,
    detail: String,
    canSubmit: Boolean,
    onToggleReason: (MemberReportReason) -> Unit,
    onDetailChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = white(),
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
        dragHandle = {
            // 시안: 시트 위 16dp 에 32x4 손잡이, 제목은 시트 위에서 52dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .background(grey600(), RoundedCornerShape(100.dp))
                )
            }
        },
    ) {
        MemberReportSheetContent(
            selectedReasons = selectedReasons,
            detail = detail,
            canSubmit = canSubmit,
            onToggleReason = onToggleReason,
            onDetailChange = onDetailChange,
            onCancel = onCancel,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun MemberReportSheetContent(
    selectedReasons: ImmutableSet<MemberReportReason>,
    detail: String,
    canSubmit: Boolean,
    onToggleReason: (MemberReportReason) -> Unit,
    onDetailChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding()
    ) {
        UText(
            text = AppStrings.MEMBER_REPORT_TITLE,
            style = UmcTypographyTokens.Title3Bold,
            color = grey950()
        )
        Spacer(modifier = Modifier.height(8.dp))
        UText(
            text = AppStrings.MEMBER_REPORT_SUBTITLE,
            style = UmcTypographyTokens.Subheadline,
            color = grey600()
        )

        Spacer(modifier = Modifier.height(8.dp))

        //신고 사유 (여러 개 선택). 한 줄 56dp, 마지막 '기타' 아래에도 구분선을 둔다
        MemberReportReason.entries.forEach { reason ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .toggleable(
                            value = reason in selectedReasons,
                            role = Role.Checkbox,
                            onValueChange = { onToggleReason(reason) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UText(
                        text = reason.label,
                        style = UmcTypographyTokens.Body,
                        color = grey950(),
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = reason in selectedReasons,
                        onCheckedChange = null,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = main500(),
                            uncheckedColor = grey400(),
                            checkmarkColor = white()
                        )
                    )
                }
                HorizontalDivider(color = grey200())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //상세 신고 내용 (필수)
        UText(
            text = AppStrings.MEMBER_REPORT_DETAIL_TITLE,
            style = UmcTypographyTokens.CalloutBold,
            color = grey950()
        )
        UTextField(
            value = detail,
            onValueChange = onDetailChange,
            placeholder = AppStrings.MEMBER_REPORT_DETAIL_PLACEHOLDER,
            textStyle = UmcTypographyTokens.Body,
            backgroundColor = white(),
            strokeColor = grey200(),
            verticalAlignment = Alignment.Top,
            acceptInput = { it.length <= MEMBER_REPORT_DETAIL_MAX_LENGTH },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        //메시지 신고 안내
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(grey100(), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 24dp 아이콘 영역 안에 20dp 아이콘
            Icon(
                painter = painterResource(id = R.drawable.ic_error_filled),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier
                    .padding(2.dp)
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            UText(
                text = AppStrings.MEMBER_REPORT_MESSAGE_GUIDE,
                style = UmcTypographyTokens.Subheadline,
                color = grey700()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            UButton(
                text = AppStrings.MEMBER_REPORT_CANCEL,
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                backgroundColor = grey100(),
                textColor = grey700(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 8.dp,
            )
            UButton(
                text = AppStrings.MEMBER_REPORT_SUBMIT,
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                backgroundColor = if (canSubmit) red100() else grey100(),
                textColor = if (canSubmit) red500() else grey300(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 8.dp,
            )
        }
    }
}

private val previewUserInfo = UserInfo(
    id = 2,
    name = "홍길동",
    nickname = "길동이",
    email = "example@email.com",
    schoolName = "중앙대학교",
    profile = ProfileInfo(
        id = 2,
        linkedIn = "https://linkedin.com/in/yourprofile",
        instagram = "https://instagram.com/yourid",
        github = "https://github.com/",
        blog = "https://yourblog.com",
        personal = "https://yourwebsite.com",
    ),
)

@PreviewScreenSizes
@Composable
private fun MemberProfileScreenPreview() {
    UmcTheme {
        MemberProfileScreen(
            uiState = MemberProfileUiState(
                userInfo = previewUserInfo,
                gisuPartText = "12기/PM",
                histories = persistentListOf(
                    UserActiveItem("12기", "Web Part", "파트장"),
                    UserActiveItem("12기", "Web Part", "파트장"),
                ),
                isLoading = false,
                isHistoryLoading = false,
            ),
            onBackClick = {},
            onMenuClick = {},
            onDismissMenu = {},
            onBlockMenuClick = {},
            onReportMenuClick = {},
            onRetryClick = {},
        )
    }
}

/** 시안 12: 값이 없는(비공개) 항목은 '비공개 항목이에요.' */
@Preview(showBackground = true, widthDp = 412, heightDp = 1500)
@Composable
private fun MemberProfileScreenPrivatePreview() {
    UmcTheme {
        MemberProfileScreen(
            uiState = MemberProfileUiState(
                userInfo = previewUserInfo.copy(
                    profile = previewUserInfo.profile.copy(blog = "", instagram = "", personal = "")
                ),
                gisuPartText = "12기/PM",
                histories = persistentListOf(UserActiveItem("12기", "Web Part", "파트장")),
                isLoading = false,
                isHistoryLoading = false,
            ),
            onBackClick = {},
            onMenuClick = {},
            onDismissMenu = {},
            onBlockMenuClick = {},
            onReportMenuClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun MemberReportSheetPreview() {
    UmcTheme {
        MemberReportSheetContent(
            selectedReasons = persistentSetOf(MemberReportReason.IMPERSONATION),
            detail = "",
            canSubmit = false,
            onToggleReason = {},
            onDetailChange = {},
            onCancel = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberProfileMenuPreview() {
    UmcTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberProfileMenuContent(isBlocked = false, isBlockMenuEnabled = true, onBlockClick = {}, onReportClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberProfileConfirmDialogPreview() {
    UmcTheme {
        MemberProfileConfirmDialog(
            title = AppStrings.MEMBER_BLOCK_CONFIRM_TITLE,
            description = AppStrings.MEMBER_BLOCK_CONFIRM_CONTENT,
            positiveText = AppStrings.MEMBER_BLOCK_CONFIRM,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
