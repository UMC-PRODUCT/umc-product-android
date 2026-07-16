package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Title2Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold

@Composable
fun GroupScheduleAddTopBar(
    canRegister: Boolean,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = null,
            tint = grey800(),
            modifier = Modifier
                .size(24.dp)
                .clickable { onBackClick() },
        )

        Spacer(Modifier.width(12.dp))

        UText(
            text = "스터디 일정 등록",
            style = Title2Bold,
            color = grey900(),
            modifier = Modifier.weight(1f),
        )

        Icon(
            painter = painterResource(R.drawable.ic_notification),
            contentDescription = null,
            tint = grey700(),
            modifier = Modifier.size(22.dp),
        )

        Spacer(Modifier.width(20.dp))

        UText(
            text = "등록",
            style = HeadlineBold,
            color = if (canRegister) indigo500() else grey300(),
            modifier = Modifier.clickable(enabled = canRegister) {
                onRegisterClick()
            },
        )
    }
}