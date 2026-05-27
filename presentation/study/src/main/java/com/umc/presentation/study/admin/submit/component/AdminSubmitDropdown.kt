package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold

@Composable
fun AdminSubmitDropdown(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(neutral100(), RoundedCornerShape(1000.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = Caption1Bold, color = neutral500())
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_dropdown_down),
            contentDescription = null,
            tint = neutral500(),
            modifier = Modifier.size(16.dp)
        )
    }
}