package com.umc.presentation.community.component.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500

/**
 * 스레드 생성/수정 화면에서 사용하는 상단바
 *
 * 왼쪽에는 뒤로가기와 화면 제목을 표시하고,
 * 오른쪽에는 등록/수정 등의 액션 버튼을 표시합니다.
 */
@Composable
fun CommunityCreateTopBar(
    title: String,
    actionText: String,
    isActionEnabled: Boolean,
    onBackClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backInteractionSource = remember {
        MutableInteractionSource()
    }

    val actionInteractionSource = remember {
        MutableInteractionSource()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        // 뒤로가기 버튼과 화면 제목
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_back,
                ),
                contentDescription = "뒤로가기",
                tint = grey950(),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = backInteractionSource,
                        indication = null,
                        onClick = onBackClick,
                    ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = title,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )
        }

        // 입력 조건 충족 여부에 따라 활성화되는 우측 액션 버튼
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 64.dp, height = 48.dp)
                .clickable(
                    interactionSource = actionInteractionSource,
                    indication = null,
                    enabled = isActionEnabled,
                    onClick = onActionClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            UText(
                text = actionText,
                style = UmcTypographyTokens.CalloutBold,
                color = if (isActionEnabled) {
                    indigo500()
                } else {
                    grey400()
                },
            )
        }
    }
}