package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body

@Composable
fun GroupScheduleMemoBox(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = Body.copy(color = neutral800()),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(neutral000(), RoundedCornerShape(8.dp))
            .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart,
            ) {
                if (value.isBlank()) {
                    UText(
                        text = placeholder,
                        style = Body,
                        color = neutral400(),
                    )
                }
                innerTextField()
            }
        },
    )
}