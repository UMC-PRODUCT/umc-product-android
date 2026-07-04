package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.primary500

@Composable
fun AdminStudyGroupCreateCard(
    onClick: () -> Unit,
) {
    UText(
        text = "스터디 그룹 생성하기",
        style = SubheadlineBold,
        color = primary500(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}