package com.umc.presentation.community.component.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.red100
import com.umc.component.theme.red500

/**
 * 스레드 수정 화면에서 사용하는 삭제 버튼
 *
 * 클릭 시 상위 화면에서 전달받은 스레드 삭제 로직을 실행합니다.
 */
@Composable
fun CommunityDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .then(
                Modifier.clip(RoundedCornerShape(8.dp))
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 삭제 버튼 배경
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(red100()),
        )

        UText(
            text = "스레드 삭제하기",
            style = UmcTypographyTokens.HeadlineBold,
            color = red500(),
        )
    }
}