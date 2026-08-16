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
import com.umc.component.theme.grey000
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.presentation.community.model.CommunityThreadUiModel

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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 370.dp),
            shape = RoundedCornerShape(12.dp),
            color = grey000(),
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
                UText(
                    text = thread.title,
                    style = UmcTypographyTokens.Title3Bold,
                    color = grey950(),
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                // 내가 작성한 스레드일 때만 편집 메뉴 표시
                if (thread.isMine) {
                    CommunityThreadMenuItem(
                        text = "편집",
                        onClick = {
                            onEditClick()
                            onDismissRequest()
                        },
                    )
                }

                CommunityThreadMenuItem(
                    text = "나가기",
                    textColor = grey950(),
                    onClick = onLeaveClick,
                )
            }
        }
    }
}

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