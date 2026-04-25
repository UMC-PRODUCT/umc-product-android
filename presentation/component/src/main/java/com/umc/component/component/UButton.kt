package com.umc.component.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000

/**
 * UMC 공용 버튼 컴포넌트.
 *
 * @param text 버튼에 표시할 텍스트
 * @param onClick 클릭 콜백
 * @param modifier 크기·위치 등 외부 레이아웃 지정. 기본값은 콘텐츠 크기에 맞게 wrap
 * @param enabled false 이면 클릭 불가 & ripple 비활성화. backgroundColor가 그대로 유지되므로
 *   비활성 색상을 별도로 지정하려면 호출부에서 backgroundColor를 조건부로 넘겨야 함
 * @param backgroundColor 기본(비누름) 배경색
 * @param pressedColor 누름 상태의 배경색. 기본값은 backgroundColor와 동일(효과 없음)
 * @param textColor 텍스트 색상
 * @param textStyle 텍스트 스타일. 기본값은 SubheadlineBold
 * @param cornerRadius 모서리 둥글기. 기본값 8dp
 * @param borderWidth 테두리 두께. 0dp 이면 테두리 없음
 * @param borderColor 테두리 색상. borderWidth > 0 일 때만 적용됨
 * @param contentPadding 텍스트·아이콘과 버튼 테두리 사이의 내부 여백.
 *   기본값 PaddingValues(0.dp) — 지정하지 않으면 여백 없이 콘텐츠 크기 그대로 렌더링
 * @param prevIcon 텍스트 왼쪽에 표시할 아이콘 (선택)
 * @param prevIconTint prevIcon 틴트 색상. null 이면 원본 색상 유지
 * @param topIcon 텍스트 위에 표시할 아이콘 (선택)
 * @param topIconTint topIcon 틴트 색상. null 이면 원본 색상 유지
 */
@Composable
fun UButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color.Black,
    pressedColor: Color = backgroundColor,
    textColor: Color = neutral000(),
    textStyle: TextStyle = UmcTypographyTokens.SubheadlineBold,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    prevIcon: Painter? = null,
    prevIconTint: Color? = null,
    topIcon: Painter? = null,
    topIconTint: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentBackground = if (isPressed && enabled) pressedColor else backgroundColor

    // defaultMinSize(0.dp)로 Material3 Button 기본 최소 크기(58×40dp)를 제거.
    // modifier가 크기를 지정하지 않으면 콘텐츠 크기에 맞게 줄어듦
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = currentBackground,
            disabledContainerColor = backgroundColor,
            contentColor = textColor,
            disabledContentColor = textColor,
        ),
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        modifier = modifier.defaultMinSize(minWidth = 0.dp, minHeight = 0.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (topIcon != null) {
                Icon(
                    painter = topIcon,
                    contentDescription = null,
                    tint = topIconTint ?: Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prevIcon != null) {
                    Icon(
                        painter = prevIcon,
                        contentDescription = null,
                        tint = prevIconTint ?: Color.Unspecified,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    style = textStyle,
                    color = textColor,
                )
            }
        }
    }
}
