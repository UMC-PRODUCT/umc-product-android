package com.umc.presentation.community.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.indigo100
import com.umc.presentation.community.R

/**
 * 카테고리 아이콘
 *
 * Figma `Icon/Category`를 구현합니다.
 * indigo100 배경의 48dp 상자 가운데에 24dp 아이콘을 원본 색상 그대로 표시합니다.
 *
 * @param iconRes 표시할 24dp 아이콘 리소스
 */
@Composable
fun CommunityCategoryIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = indigo100(),
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * 카테고리 아이콘 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityCategoryIconPreview() {
    CommunityCategoryIcon(
        iconRes = R.drawable.ic_category_study,
        modifier = Modifier.padding(16.dp),
    )
}
