package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*

@Composable
fun GroupCreateTextField(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        UText(
            text = title,
            style = UmcTypographyTokens.SubheadlineBold,
            color = grey800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}