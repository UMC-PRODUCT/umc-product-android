package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
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
fun AdminStudyGroupCreateCard(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
            .background(grey000(), RoundedCornerShape(8.dp))
            .border(1.dp, grey200(), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_book_filled),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        UText(
            text = "스터디 그룹 생성하기",
            style = Caption1Bold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(18.dp)
        )
    }
}