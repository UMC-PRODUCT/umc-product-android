package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.component.R
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.black
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey950
import com.umc.component.theme.grey600
import com.umc.component.theme.main500
import com.umc.component.theme.white
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMemberState
import com.umc.presentation.community.chatting.CommunityDraftMention
import com.umc.presentation.community.chatting.communityMentionQuery
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CommunityChatInputBar(
    value: String,
    enabled: Boolean,
    replyingMessage: CommunityThreadMessage?,
    onValueChange: (String, Int, Int) -> Unit,
    onCamera: () -> Unit,
    onSend: () -> Unit,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier,
    onInputFocusChanged: (Boolean) -> Unit = {},
    onInputClick: () -> Unit = {},
    mentionMembers: ImmutableList<CommunityThreadMember> = persistentListOf(),
    draftMentions: ImmutableList<CommunityDraftMention> = persistentListOf(),
    onMentionSelected: (String, Int, Int) -> Unit = { _, _, _ -> },
) {
    var isFocused by remember { mutableStateOf(false) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var cursorAfterMention by remember { mutableStateOf<Int?>(null) }
    val focusRequester = remember { FocusRequester() }
    val mentionColor = main500()
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, TextRange((cursorAfterMention ?: value.length).coerceIn(0, value.length)))
            cursorAfterMention = null
        }
    }
    val query = if (isFocused && enabled && fieldValue.selection.collapsed && fieldValue.text == value) {
        communityMentionQuery(value, fieldValue.selection.end)?.takeUnless { query ->
            draftMentions.any { query.start >= it.start && query.end <= it.end }
        }
    } else null
    val suggestions = remember(query, mentionMembers) {
        if (query == null) emptyList() else mentionMembers.filter {
            it.state == CommunityThreadMemberState.ACTIVE && it.name.isNotBlank() &&
                it.name.contains(query.text, ignoreCase = true)
        }.sortedBy { it.name }
    }
    val isReplying = replyingMessage != null
    val containerShape = if (isReplying) {
        RoundedCornerShape(20.dp)
    } else {
        RoundedCornerShape(1000.dp)
    }

    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().imePadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
    ) {
        if (query != null && suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = white(),
                border = BorderStroke(1.dp, grey200()),
                shadowElevation = 3.dp,
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(suggestions, key = { it.memberId }) { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val replacement = "@${member.name} "
                                cursorAfterMention = query.start + replacement.length
                                onMentionSelected(member.memberId, query.start, query.end)
                                focusRequester.requestFocus()
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CommunityChatProfileImage(member.profileImageUrl, useThreadStyle = true)
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = member.name,
                                    modifier = Modifier.weight(1f, fill = false),
                                    color = black(), style = UmcTypographyTokens.Callout,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                                member.part?.let { part ->
                                    Text(
                                        text = communityChatPartTag(part).first,
                                        color = grey600(), style = UmcTypographyTokens.Footnote,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isFocused && !isReplying) 10.dp else 0.dp,
                ),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = containerShape,
                color = white(),
                border = BorderStroke(1.dp, grey200()),
                shadowElevation = when {
                    isReplying -> 7.dp
                    isFocused -> 3.dp
                    else -> 0.dp
                },
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
                            .heightIn(min = if (isReplying) 40.dp else 56.dp)
                            .padding(horizontal = if (isReplying) 0.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!isReplying) {
                            IconButton(onClick = onCamera) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_photo),
                                    contentDescription = AppStrings.CHAT_CD_ATTACH_PHOTO,
                                    tint = com.umc.component.theme.grey600(),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty() && (!isFocused || isReplying)) {
                                Text(
                                    text = if (isReplying) {
                                        AppStrings.CHAT_REPLY_PLACEHOLDER
                                    } else {
                                        AppStrings.CHAT_MESSAGE_PLACEHOLDER
                                    },
                                    color = grey400(),
                                    style = UmcTypographyTokens.Body,
                                )
                            }

                            BasicTextField(
                                value = fieldValue,
                                onValueChange = {
                                    val before = fieldValue
                                    cursorAfterMention = null
                                    fieldValue = it
                                    if (it.text != before.text) {
                                        val prefix = before.text.commonPrefixWith(it.text).length
                                            .coerceAtMost(minOf(before.selection.min, it.selection.min))
                                        val suffix = before.text.substring(prefix).commonSuffixWith(it.text.substring(prefix)).length
                                            .coerceAtMost(minOf(before.text.length - before.selection.max, it.text.length - it.selection.max))
                                        onValueChange(it.text, prefix, before.text.length - suffix)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        isFocused = it.isFocused
                                        onInputFocusChanged(it.isFocused)
                                    }
                                    .pointerInput(onInputClick) {
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                            onInputClick()
                                        }
                                    },
                                enabled = enabled,
                                visualTransformation = VisualTransformation { text ->
                                    val ranges = draftMentions.filter {
                                        it.start >= 0 && it.end <= text.length && it.start < it.end &&
                                            text.text.substring(it.start, it.end) == "@${it.label}"
                                    }.map { it.start until it.end }
                                    TransformedText(
                                        communityMentionText(text.text, ranges, mentionColor),
                                        OffsetMapping.Identity,
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = black(),
                                    fontFamily = UmcTypographyTokens.Body.fontFamily,
                                    fontSize = 17.sp,
                                    lineHeight = 21.sp,
                                ),
                                cursorBrush = SolidColor(
                                    if (isReplying) main500() else black(),
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
                                    enabled && value.isNotBlank() -> main500()
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
                text = if (message.type == CommunityMessageType.IMAGE) {
                    // 방금 보낸 사진은 content 에 로컬 URI 가 들어 있어 그대로 노출하지 않는다.
                    AppStrings.CHAT_MESSAGE_CONTENT
                } else {
                    message.content.orEmpty().ifBlank { AppStrings.CHAT_MESSAGE_CONTENT }
                },
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
                painterResource(R.drawable.ic_clear_circle),
                contentDescription = AppStrings.CHAT_CD_CANCEL_REPLY,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
