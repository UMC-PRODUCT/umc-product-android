package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*

@Composable
fun GroupCreateSelectRow(
    title: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        UText(
            text = title,
            style = UmcTypographyTokens.SubheadlineBold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            color = neutral000(),
            border = BorderStroke(1.dp, neutral200())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UText(
                    text = value.ifBlank { placeholder },
                    style = UmcTypographyTokens.Subheadline,
                    color = if (value.isBlank()) neutral400() else neutral800(),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    tint = neutral500(),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}