package com.umc.presentation.community.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green100
import com.umc.component.theme.green200
import com.umc.component.theme.green500
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo200
import com.umc.component.theme.indigo500
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow200
import com.umc.component.theme.yellow500
import com.umc.presentation.community.model.CommunityCategory

/**
 * 스레드의 카테고리를 표시하는 칩
 *
 * 카테고리에 따라 배경색, 테두리색, 텍스트 색상을 다르게 표시합니다.
 * ALL, UNREAD는 필터에서만 사용하는 카테고리이므로 칩을 표시하지 않습니다.
 */
@Composable
fun CommunityCategoryChip(
    category: CommunityCategory,
    modifier: Modifier = Modifier,
) {
    // 전체 및 안읽음 카테고리는 스레드 카드에 표시하지 않음
    if (
        category == CommunityCategory.ALL ||
        category == CommunityCategory.UNREAD
    ) {
        return
    }

    val backgroundColor: Color
    val borderColor: Color
    val textColor: Color

    // 카테고리별 칩 색상 설정
    when (category) {
        CommunityCategory.STUDY -> {
            backgroundColor = indigo100()
            borderColor = indigo200()
            textColor = indigo500()
        }

        CommunityCategory.PROJECT -> {
            backgroundColor = yellow100()
            borderColor = yellow200()
            textColor = yellow500()
        }

        CommunityCategory.QNA -> {
            backgroundColor = grey100()
            borderColor = grey200()
            textColor = grey500()
        }

        CommunityCategory.FREE -> {
            backgroundColor = green100()
            borderColor = green200()
            textColor = green500()
        }

        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> return
    }

    // 카테고리 칩 UI
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor,
        ),
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 2.dp,
            ),
            contentAlignment = Alignment.Center,
        ) {
            UText(
                text = category.label,
                style = UmcTypographyTokens.Caption1Bold,
                color = textColor,
            )
        }
    }
}