package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UButton
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.main500

/**
 * 커뮤니티 화면에서 사용하는 기본 액션 버튼
 *
 * Figma `Action Button`(main500 배경, 8dp 모서리, Callout-emphasized 텍스트)을
 * 공통 컴포넌트인 [UButton]으로 구성.
 * 비활성화 상태에서는 grey100 배경과 grey300 텍스트를 사용.
 * Headline-emphasized 텍스트를 쓰는 화면(Empty 화면 등)은 [textStyle]로 바꿔 사용.
 */
@Composable
fun CommunityActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = UmcTypographyTokens.CalloutBold,
) {
    UButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        backgroundColor = if (enabled) main500() else grey100(),
        textColor = if (enabled) grey50() else grey300(),
        textStyle = textStyle,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 13.dp),
    )
}

/**
 * 커뮤니티 액션 버튼 Preview
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityActionButtonPreview() {
    Column(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .padding(16.dp),
    ) {
        CommunityActionButton(
            text = "카테고리 분류",
            onClick = {},
        )

        CommunityActionButton(
            text = "카테고리 분류",
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
