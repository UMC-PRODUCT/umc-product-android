package com.example.presentation.act.normal.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900

@Composable
fun AttendanceReasonDialog(
    reason: String,
    onReasonChange: (String) -> Unit,
    onReasonFocusChange: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        AttendanceReasonDialogContent(
            reason = reason,
            onReasonChange = onReasonChange,
            onReasonFocusChange = onReasonFocusChange,
            onDismissRequest = onDismissRequest,
            onSubmit = onSubmit,
            modifier = modifier
        )
    }
}

@Composable
private fun AttendanceReasonDialogContent(
    reason: String,
    onReasonChange: (String) -> Unit,
    onReasonFocusChange: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isReasonFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(neutral000())
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = AppStrings.ATTENDANCE_REASON_DIALOG_TITLE,
                style = Title3Bold,
                color = neutral800()
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = AppStrings.ATTENDANCE_REASON_GUIDE,
                style = Subheadline,
                color = neutral600(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        UText(
            text = AppStrings.ATTENDANCE_WRITE_REASON,
            style = BodyBold,
            color = neutral800(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(neutral000())
                .border(
                    width = 1.dp,
                    color = neutral300(),
                    shape = RoundedCornerShape(8.dp)
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            BasicTextField(
                value = reason,
                onValueChange = onReasonChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onReasonFocusChange(it.isFocused) }
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                singleLine = true,
                textStyle = Body,
                cursorBrush = SolidColor(neutral800()),
                decorationBox = { innerTextField ->
                    if (reason.isBlank()) {
                        UText(
                            text = AppStrings.ATTENDANCE_WRITE_REASON_DESCRIPTION,
                            style = Body,
                            color = neutral400()
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UButton(
                text = AppStrings.COMMON_CANCEL,
                onClick = onDismissRequest,
                modifier = Modifier
                    .weight(1f),
                backgroundColor = neutral100(),
                textColor = neutral700(),
                textStyle = CalloutBold,
                cornerRadius = 8.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
            )

            UButton(
                text = AppStrings.COMMON_SUBMIT,
                onClick = { onSubmit(reason) },
                modifier = Modifier
                    .weight(1f),
                backgroundColor = neutral800(),
                textColor = neutral000(),
                textStyle = CalloutBold,
                cornerRadius = 8.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 13.dp)
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun AttendanceReasonDialogPreview() {
    UmcTheme(darkTheme = false) {
        var reason by remember { mutableStateOf("") }
        AttendanceReasonDialogContent(
            reason = reason,
            onReasonChange = { reason = it },
            onDismissRequest = {},
            onSubmit = {}
        )
    }
}
