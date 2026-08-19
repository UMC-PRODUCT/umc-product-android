package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.component.R
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.black
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.community.thread.CommunityThreadMessage

@Composable
internal fun CommunityChatInputBar(
    value: String,
    enabled: Boolean,
    replyingMessage: CommunityThreadMessage?,
    onValueChange: (String) -> Unit,
    onCamera: () -> Unit,
    onSend: () -> Unit,
    onCancelReply: () -> Unit,
) {
    val isReplying = replyingMessage != null
    val containerShape = if (isReplying) {
        RoundedCornerShape(20.dp)
    } else {
        RoundedCornerShape(1000.dp)
    }

    Surface(color = grey100()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(10.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape,
                color = white(),
                border = BorderStroke(1.dp, grey200()),
                shadowElevation = if (isReplying) 7.dp else 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isReplying) {
                                Modifier.padding(
                                    start = 20.dp,
                                    end = 12.dp,
                                    top = 16.dp,
                                    bottom = 14.dp,
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    if (replyingMessage != null) {
                        ReplyTargetRow(
                            message = replyingMessage,
                            onCancelReply = onCancelReply,
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (isReplying) 40.dp else 54.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!isReplying) {
                            IconButton(onClick = onCamera) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_photo),
                                    contentDescription = AppStrings.CHAT_CD_ATTACH_PHOTO,
                                    tint = black(),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = if (isReplying) {
                                        AppStrings.CHAT_REPLY_PLACEHOLDER
                                    } else {
                                        AppStrings.CHAT_MESSAGE_PLACEHOLDER
                                    },
                                    color = grey400(),
                                    fontSize = if (isReplying) 17.sp else 14.sp,
                                )
                            }

                            BasicTextField(
                                value = value,
                                onValueChange = onValueChange,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = enabled,
                                textStyle = LocalTextStyle.current.copy(
                                    color = black(),
                                    fontSize = if (isReplying) 17.sp else 14.sp,
                                    lineHeight = if (isReplying) 23.sp else 20.sp,
                                ),
                                cursorBrush = SolidColor(
                                    if (isReplying) indigo500() else black(),
                                ),
                                maxLines = if (isReplying) 3 else 4,
                            )
                        }

                        if (isReplying) {
                            Spacer(Modifier.size(8.dp))
                        }

                        IconButton(
                            onClick = onSend,
                            enabled = enabled && value.isNotBlank(),
                            modifier = if (isReplying) Modifier.size(40.dp) else Modifier,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = if (isReplying) {
                                    AppStrings.CHAT_CD_REPLY_SEND
                                } else {
                                    AppStrings.CHAT_CD_SEND
                                },
                                modifier = if (isReplying) Modifier.size(34.dp) else Modifier,
                                tint = when {
                                    enabled && value.isNotBlank() -> indigo500()
                                    isReplying -> grey400()
                                    else -> grey300()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyTargetRow(
    message: CommunityThreadMessage,
    onCancelReply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = AppStrings.CHAT_REPLY_TO_FORMAT.format(
                    message.senderName.orEmpty().ifBlank { AppStrings.CHAT_ME },
                ),
                color = grey950(),
                style = UmcTypographyTokens.SubheadlineBold,
            )
            Text(
                text = message.content.orEmpty().ifBlank { AppStrings.CHAT_MESSAGE_CONTENT },
                color = grey400(),
                style = UmcTypographyTokens.Footnote,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = onCancelReply,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = AppStrings.CHAT_CD_CANCEL_REPLY,
                tint = grey400(),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
