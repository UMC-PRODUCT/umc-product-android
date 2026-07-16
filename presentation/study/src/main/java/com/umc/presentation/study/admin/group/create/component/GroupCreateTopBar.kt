package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*

@Composable
fun GroupCreateTopBar(
    isRegisterEnabled: Boolean,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = grey800(),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        UText(
            text = "스터디 그룹 생성",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        UText(
            text = "등록",
            style = UmcTypographyTokens.SubheadlineBold,
            color = if (isRegisterEnabled) indigo500() else grey400(),
            modifier = Modifier
                .padding(start = 12.dp)
                .padding(8.dp) // 터치 영역 확보
                .clickable(
                    enabled = isRegisterEnabled,
                    onClick = onRegisterClick
                )
        )
    }
}

