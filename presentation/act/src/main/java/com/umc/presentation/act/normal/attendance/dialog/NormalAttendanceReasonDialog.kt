package com.umc.presentation.act.normal.attendance.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800

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
    val reasonInteractionSource = remember { MutableInteractionSource() }
    val isReasonFocused by reasonInteractionSource.collectIsFocusedAsState()

    LaunchedEffect(isReasonFocused) {
        onReasonFocusChange(isReasonFocused)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(grey000())
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
                color = grey800()
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = AppStrings.ATTENDANCE_REASON_GUIDE,
                style = Subheadline,
                color = grey600(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        UText(
            text = AppStrings.ATTENDANCE_WRITE_REASON,
            style = BodyBold,
            color = grey800(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = reason,
            onValueChange = onReasonChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = AppStrings.ATTENDANCE_WRITE_REASON_DESCRIPTION,
            placeholderColor = grey400(),
            textColor = grey800(),
            textStyle = Body,
            backgroundColor = grey000(),
            strokeColor = grey300(),
            focusStrokeColor = grey300(),
            interactionSource = reasonInteractionSource
        )

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
                backgroundColor = grey100(),
                textColor = grey700(),
                textStyle = CalloutBold,
                cornerRadius = 8.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
            )

            UButton(
                text = AppStrings.COMMON_SUBMIT,
                onClick = { onSubmit(reason) },
                modifier = Modifier
                    .weight(1f),
                backgroundColor = grey800(),
                textColor = grey000(),
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
