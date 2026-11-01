package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.main500
import com.umc.component.theme.red400
import com.umc.component.theme.white

@Composable
internal fun CommunityChatEditInputBar(
    messageId: String,
    originalText: String,
    value: String,
    saving: Boolean,
    canSave: Boolean,
    error: String?,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onInputFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val currentOnFocusChanged by rememberUpdatedState(onInputFocusChanged)
    var fieldValue by remember(messageId) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }
    LaunchedEffect(messageId) {
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboard?.show()
    }
    DisposableEffect(Unit) {
        onDispose { currentOnFocusChanged(false) }
    }

    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = white(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, grey200()),
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        UText(AppStrings.CHAT_EDIT_MESSAGE_TITLE, color = grey950(), style = UmcTypographyTokens.SubheadlineBold)
                        UText(
                            originalText,
                            color = grey400(),
                            style = UmcTypographyTokens.Footnote,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onCancel, enabled = !saving) {
                        Icon(
                            painterResource(R.drawable.ic_clear_circle),
                            contentDescription = "메시지 수정 취소",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BasicTextField(
                        value = fieldValue,
                        onValueChange = {
                            fieldValue = it
                            onValueChange(it.text)
                        },
                        enabled = !saving,
                        modifier = Modifier.weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { currentOnFocusChanged(it.isFocused) },
                        textStyle = UmcTypographyTokens.Body.copy(color = grey950()),
                        cursorBrush = SolidColor(main500()),
                        maxLines = 4,
                    )
                    IconButton(onClick = onSave, enabled = canSave) {
                        if (saving) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                painterResource(R.drawable.ic_send),
                                contentDescription = "메시지 수정 저장",
                                tint = if (canSave) main500() else grey400(),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                if (saving) UText(AppStrings.CHAT_EDIT_CONFIRMING, color = grey600(), style = UmcTypographyTokens.Footnote)
                error?.let { UText(it, color = red400(), style = UmcTypographyTokens.Footnote) }
            }
        }
    }
}
