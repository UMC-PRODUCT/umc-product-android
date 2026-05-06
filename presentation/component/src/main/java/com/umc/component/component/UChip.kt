package com.umc.component.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.primary500
import com.umc.component.R

/**
 * UChip: 기존 XML 기반 UChip 커스텀 뷰의 속성을 계승한 컴포저블
 *
 * @param text 칩에 표시할 텍스트 (XML: text)
 * @param modifier 레이아웃 수정을 위한 Modifier
 * @param backgroundColor 칩 배경색 (XML: backgroundColor) - 기본 primary500()
 * @param textColor 텍스트 및 닫기 아이콘 색상 (XML: textColor) - 기본 neutral000()
 * @param textStyle 텍스트 스타일 (XML: textAppearance) - 기본 UmcTypographyTokens.SubheadlineBold
 * @param borderWidth 외곽선 두께 (XML: borderWidth) - 기본 0.dp
 * @param borderColor 외곽선 색상 (XML: borderColor) - 기본 Color.Transparent
 * @param showCloseIcon 닫기(X) 버튼 표시 여부 (XML: showCloseIcon) - 기본 false
 * @param nextIcon 후위 아이콘 리소스 ID (XML: nextIcon) - 기본 false
 * @param onClick 칩 자체 클릭 리스너
 * @param onCloseClick 닫기 버튼 클릭 리스너 (XML: onCloseClickListener)
 */

@Composable
fun UChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = primary500(),
    textColor: Color = neutral000(),
    textStyle: TextStyle = UmcTypographyTokens.SubheadlineBold,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    showCloseIcon: Boolean = false,
    nextIcon: Int? = null,
    onClick: (() -> Unit)? = null,
    onCloseClick: ((String) -> Unit)? = null
) {
    // 칩의 가시성 상태 관리
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(100.dp),
            color = backgroundColor,
            contentColor = textColor,
            border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
            onClick = { onClick?.invoke() },
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                UText(
                    text = text,
                    style = textStyle,
                    color = textColor
                )

                // 닫기 버튼 (X버튼 보여주기 유무)
                if (showCloseIcon) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isVisible = false
                                onCloseClick?.invoke(text)
                            }
                    )
                }

                //후위 버튼 (image_next) - 닫기 버튼과 배타적으로 사용하거나 순서대로 배치
                if (!showCloseIcon && nextIcon != null) {
                    Icon(
                        painter = painterResource(id = nextIcon),
                        contentDescription = "Next",
                        tint = textColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    }
}