package com.umc.presentation.community.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.white
import com.umc.component.theme.grey800
import androidx.compose.ui.tooling.preview.Preview
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.grey950
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

/**
 * 스레드 롱클릭 시 표시되는 관리 메뉴 다이얼로그
 *
 * 스레드 고정, 알림 설정, 편집, 나가기 기능을 제공합니다.
 * 편집 메뉴는 방장(OWNER) 또는 관리자(ADMIN)인 경우에만 표시됩니다.
 */
@Composable
fun CommunityThreadMenuDialog(
    thread: CommunityThreadUiModel,
    onDismissRequest: () -> Unit,
    onTogglePinClick: () -> Unit,
    onToggleNotificationClick: () -> Unit,
    onEditClick: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        CommunityThreadMenuContent(
            thread = thread,
            onDismissRequest = onDismissRequest,
            onTogglePinClick = onTogglePinClick,
            onToggleNotificationClick = onToggleNotificationClick,
            onEditClick = onEditClick,
            onLeaveClick = onLeaveClick,
        )
    }
}

/**
 * 스레드 관리 메뉴 다이얼로그의 내용
 *
 * Dialog는 별도 창에 그려져 정적 프리뷰에 나타나지 않으므로 내용을 분리해 둡니다.
 */
@Composable
private fun CommunityThreadMenuContent(
    thread: CommunityThreadUiModel,
    onDismissRequest: () -> Unit,
    onTogglePinClick: () -> Unit,
    onToggleNotificationClick: () -> Unit,
    onEditClick: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .widthIn(max = 370.dp),
        shape = RoundedCornerShape(12.dp),
        color = white(),
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 30.dp,
                bottom = 16.dp,
            ),
        ) {
            // 선택한 스레드 제목
            UText(
                text = thread.title,
                style = UmcTypographyTokens.Title3Bold,
                color = grey950(),
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 스레드 고정 또는 고정 해제
            CommunityThreadMenuItem(
                text = if (thread.isPinned) {
                    "고정 해제"
                } else {
                    "고정"
                },
                onClick = {
                    onTogglePinClick()
                    onDismissRequest()
                },
            )

            // 스레드 알림 켜기 또는 끄기
            CommunityThreadMenuItem(
                text = if (thread.isNotificationEnabled) {
                    "알림 끄기"
                } else {
                    "알림 켜기"
                },
                onClick = {
                    onToggleNotificationClick()
                    onDismissRequest()
                },
            )

            // 방장 또는 관리자일 때만 편집 메뉴 표시
            if (thread.canEdit) {
                CommunityThreadMenuItem(
                    text = "편집",
                    onClick = {
                        onEditClick()
                        onDismissRequest()
                    },
                )
            }

            // 현재 스레드에서 나가기
            CommunityThreadMenuItem(
                text = "나가기",
                textColor = grey950(),
                onClick = onLeaveClick,
            )
        }
    }
}

/**
 * 스레드 관리 메뉴에서 사용하는 공통 메뉴 항목
 */
@Composable
private fun CommunityThreadMenuItem(
    text: String,
    onClick: () -> Unit,
    textColor: Color = grey800(),
) {
    UText(
        text = text,
        style = UmcTypographyTokens.Body,
        color = textColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 0.dp,
                vertical = 15.5.dp,
            ),
    )
}

@Preview(name = "방장·관리자 (편집 메뉴 있음)", showBackground = true)
@Composable
private fun CommunityThreadMenuDialogEditablePreview() {
    CommunityThreadMenuContentPreview(canEdit = true)
}

@Preview(name = "일반 멤버 (편집 메뉴 없음)", showBackground = true)
@Composable
private fun CommunityThreadMenuDialogMemberPreview() {
    CommunityThreadMenuContentPreview(canEdit = false)
}

@Composable
private fun CommunityThreadMenuContentPreview(canEdit: Boolean) {
    UmcTheme {
        CommunityThreadMenuContent(
            thread = CommunityThreadUiModel(
                id = "1",
                title = "iOS 3주차 과제 인증방",
                contentPreview = "다들 과제 인증 완료했나요?",
                category = CommunityCategory.STUDY,
                dayText = "오늘",
                isPinned = false,
                isNotificationEnabled = true,
                isMine = canEdit,
                canEdit = canEdit,
                isJoined = true,
            ),
            onDismissRequest = {},
            onTogglePinClick = {},
            onToggleNotificationClick = {},
            onEditClick = {},
            onLeaveClick = {},
        )
    }
}