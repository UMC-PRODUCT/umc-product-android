package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral800

@Composable
fun AdminStudyGroupHeader() {
    UText(
        text = "활동 관리",
        style = Title3Bold,
        color = neutral800(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
}