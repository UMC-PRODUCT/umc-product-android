package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold

@Composable
fun GroupScheduleInfoCard(
    groupTitle: String,
    groupPart: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(grey100(), RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notification),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(22.dp),
        )

        Spacer(Modifier.width(8.dp))

        UText(
            text = groupTitle,
            style = HeadlineBold,
            color = grey900(),
        )

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .background(indigo100(), RoundedCornerShape(4.dp))
                .border(1.dp, indigo100(), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            UText(
                text = groupPart,
                style = Caption1Bold,
                color = indigo500(),
            )
        }
    }
}