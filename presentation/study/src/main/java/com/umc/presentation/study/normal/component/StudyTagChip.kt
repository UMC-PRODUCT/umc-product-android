package com.umc.presentation.study.normal.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*

/**
 * Week N / 플랫폼 표시용 태그 칩
 *
 * 예시: [Week 1] [Github]
 */
@Composable
fun StudyTagChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = neutral000(),
        modifier = modifier.border(1.dp, neutral200(), RoundedCornerShape(6.dp))
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.FootnoteBold,
            color = neutral700(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}