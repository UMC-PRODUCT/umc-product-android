package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun AdminStudyGroupScheduleButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .border(1.dp, indigo500(), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_calendar_color),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(15.dp)
        )

        Spacer(Modifier.width(4.dp))

        UText(
            text = "스터디 일정 등록하기",
            style = Caption1Bold,
            color = grey700()
        )
    }
}