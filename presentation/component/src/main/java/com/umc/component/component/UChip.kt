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
import com.umc.component.theme.grey000
import com.umc.component.theme.indigo500
import com.umc.component.R

/**
 * 선택 상태나 분류 정보를 표시하는 공용 칩 컴포넌트입니다.
 *
 * [onClick]이 null이면 표시 전용으로 동작하며, [showCloseIcon]을 사용하면 닫기 액션을 제공합니다.
 *
 * @param text 칩에 표시할 텍스트
 * @param modifier 크기와 배치를 지정하는 Modifier
 * @param backgroundColor 칩 배경색
 * @param textColor 텍스트와 아이콘 색상
 * @param textStyle 텍스트 스타일
 * @param cornerRadius 모서리 반경
 * @param contentPadding 칩 내부 여백
 * @param minHeight 칩의 최소 높이
 * @param borderWidth 테두리 두께
 * @param borderColor 테두리 색상
 * @param showCloseIcon 닫기 아이콘 표시 여부
 * @param nextIcon 텍스트 뒤에 표시할 drawable 리소스
 * @param onClick 칩 클릭 콜백
 * @param onCloseClick 닫기 아이콘 클릭 콜백
 */
@Composable
fun UChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = indigo500(),
    textColor: Color = grey000(),
    textStyle: TextStyle = UmcTypographyTokens.SubheadlineBold,
    cornerRadius: Dp = 100.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 7.dp),
    minHeight: Dp? = null,
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
            shape = RoundedCornerShape(cornerRadius),
            color = backgroundColor,
            contentColor = textColor,
            border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
            onClick = { onClick?.invoke() },
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .then(if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier)
                    .padding(contentPadding),
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

                // 후행 아이콘은 닫기 버튼과 함께 사용할 수 있습니다.
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
