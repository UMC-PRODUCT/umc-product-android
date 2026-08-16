package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
) {
    Surface(color = grey100()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(10.dp),
        ) {
            if (replyingMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = white(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, grey200()),
                    shadowElevation = 7.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 132.dp)
                            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = AppStrings.CHAT_REPLY_TO_FORMAT.format(
                                    replyingMessage.senderName.orEmpty().ifBlank { AppStrings.CHAT_ME },
                                ),
                                color = grey950(),
                                style = UmcTypographyTokens.SubheadlineBold,
                            )
                            Text(
                                text = replyingMessage.content.orEmpty().ifBlank {
                                    AppStrings.CHAT_MESSAGE_CONTENT
                                },
                                color = grey400(),
                                style = UmcTypographyTokens.Footnote,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(7.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        AppStrings.CHAT_REPLY_PLACEHOLDER,
                                        color = grey400(),
                                        fontSize = 17.sp,
                                    )
                                }
                                BasicTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = enabled,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = black(),
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                    ),
                                    cursorBrush = SolidColor(indigo500()),
                                    maxLines = 3,
                                )
                            }
                        }
                        IconButton(
                            onClick = onSend,
                            enabled = enabled && value.isNotBlank(),
                            modifier = Modifier.align(Alignment.Top),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = AppStrings.CHAT_CD_REPLY_SEND,
                                modifier = Modifier.size(34.dp),
                                tint = if (enabled && value.isNotBlank()) indigo500() else grey400(),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(1000.dp))
                        .background(white())
                        .border(1.dp, grey200(), RoundedCornerShape(1000.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCamera) {
                        Icon(
                            painterResource(R.drawable.ic_photo),
                            contentDescription = AppStrings.CHAT_CD_ATTACH_PHOTO,
                            tint = black(),
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                AppStrings.CHAT_MESSAGE_PLACEHOLDER,
                                color = grey400(),
                                fontSize = 14.sp,
                            )
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled,
                            textStyle = LocalTextStyle.current.copy(
                                color = black(),
                                fontSize = 14.sp,
                            ),
                            cursorBrush = SolidColor(black()),
                            maxLines = 4,
                        )
                    }
                    IconButton(
                        onClick = onSend,
                        enabled = enabled && value.isNotBlank(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = AppStrings.CHAT_CD_SEND,
                            tint = if (enabled && value.isNotBlank()) indigo500() else grey300(),
                        )
                    }
                }
            }
        }
    }
}
