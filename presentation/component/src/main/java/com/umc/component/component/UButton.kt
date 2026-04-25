package com.umc.component.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens

/**
 * UButton
 *
 * @param text 버튼에 표시할 텍스트 (XML: R.styleable.UButton_text)
 * @param modifier 레이아웃 수정 및 크기 조절을 위한 Modifier
 * @param onClick 버튼 클릭 시 실행될 리스너
 * @param cardBackgroundColor 버튼의 배경 색상
 * @param textColor 텍스트 및 아이콘의 기본 색상
 * @param textAppearance 텍스트의 글꼴, 크기, 두께 등 스타일
 * @param cornerRadius 버튼 모서리의 둥근 정도
 * @param borderWidth 테두리(Stroke)의 두께
 * @param borderColor 테두리의 색상
 * @param buttonElevation 버튼이 바닥에서 떠 있는 높이/그림자
 * @param topIcon 텍스트 상단에 배치될 아이콘 리소스 ID
 * @param prevIcon 텍스트 왼쪽에 배치될 아이콘 리소스 ID
 * @param prevIconTint 상단 및 전위 아이콘에 적용할 색상
 * @param contentPadding 버튼 내부 콘텐츠와 테두리 사이의 간격
 */

@Composable
fun UButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    // 배경색 및 텍스트 색상
    cardBackgroundColor: Color = Color.Unspecified,
    textColor: Color = Color.White,
    textAppearance: TextStyle = UmcTypographyTokens.SubheadlineBold, // 기본 스타일 계승
    // 형태 관련
    cornerRadius: Dp = 4.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent, // 기본 투명
    buttonElevation: Dp = 0.dp,
    // 아이콘 관련
    topIcon: Int? = null,
    prevIcon: Int? = null,
    prevIconTint: Color? = null,
    // 패딩 (내부 간격)
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
) {

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = cardBackgroundColor,
        contentColor = textColor,
        shadowElevation = buttonElevation,
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null
    ) {

        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            topIcon?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = prevIconTint ?: textColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                prevIcon?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = prevIconTint ?: textColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }


                Text(
                    text = text,
                    style = textAppearance,
                    color = textColor
                )
            }
        }
    }
}