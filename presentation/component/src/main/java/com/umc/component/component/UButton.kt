package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.black
import com.umc.component.theme.grey000

/**
 * UMC 공용 버튼 컴포넌트입니다.
 *
 * 활동 화면의 기본 버튼, 테두리 버튼, 아이콘 버튼을 하나의 API로 제공합니다.
 * 눌림 상태에서는 [pressedColor]를 사용하며, [enabled]가 false이면 클릭과 ripple이 비활성화됩니다.
 *
 * @param text 버튼에 표시할 텍스트
 * @param onClick 버튼 클릭 콜백
 * @param modifier 크기와 배치를 지정하는 Modifier
 * @param enabled 버튼 활성화 여부
 * @param backgroundColor 기본 배경색
 * @param pressedColor 눌렀을 때의 배경색
 * @param textColor 텍스트 색상
 * @param textStyle 텍스트 스타일
 * @param cornerRadius 모서리 반경
 * @param borderWidth 테두리 두께. 0.dp이면 표시하지 않습니다.
 * @param borderColor 테두리 색상
 * @param contentPadding 버튼 내부 여백
 * @param prevIcon 텍스트 앞에 표시할 아이콘
 * @param prevIconTint 앞 아이콘 색상. null이면 원본 색상을 유지합니다.
 * @param topIcon 텍스트 위에 표시할 아이콘
 * @param topIconTint 위 아이콘 색상. null이면 원본 색상을 유지합니다.
 * @param prevIconSize 앞 아이콘 크기
 * @param prevIconMargin 앞 아이콘과 텍스트 사이 간격
 * @param topIconSize 위 아이콘 크기
 * @param endIcon 텍스트 오른쪽 끝에 표시할 아이콘
 * @param endIconTint 오른쪽 아이콘 색상. null이면 원본 색상을 유지합니다.
 * @param endIconSize 오른쪽 아이콘 크기
 */
@Composable
fun UButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = black(),
    pressedColor: Color = backgroundColor,
    textColor: Color = grey000(),
    textStyle: TextStyle = UmcTypographyTokens.SubheadlineBold,
    cornerRadius: Dp = 8.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    prevIcon: Painter? = null,
    prevIconTint: Color? = null,
    prevIconSize: DpSize = DpSize(24.dp, 24.dp),
    prevIconMargin: Dp = 8.dp,
    topIcon: Painter? = null,
    topIconTint: Color? = null,
    topIconSize: DpSize = DpSize(24.dp, 24.dp),
    endIcon: Painter? = null,
    endIconTint: Color? = null,
    endIconSize: DpSize = DpSize(24.dp, 24.dp),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentBackground = if (isPressed && enabled) pressedColor else backgroundColor
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(currentBackground)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape)
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(contentPadding),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (topIcon != null) {
                Icon(
                    painter = topIcon,
                    contentDescription = null,
                    tint = topIconTint ?: Color.Unspecified,
                    modifier = Modifier.size(topIconSize),
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Row(
                modifier = if (endIcon != null) Modifier.fillMaxWidth() else Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prevIcon != null) {
                    Icon(
                        painter = prevIcon,
                        contentDescription = null,
                        tint = prevIconTint ?: Color.Unspecified,
                        modifier = Modifier.size(prevIconSize),
                    )
                    Spacer(modifier = Modifier.width(prevIconMargin))
                }

                UText(
                    text = text,
                    style = textStyle,
                    color = textColor,
                )

                if (endIcon != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = endIcon,
                        contentDescription = null,
                        tint = endIconTint ?: Color.Unspecified,
                        modifier = Modifier.size(endIconSize),
                    )
                }
            }
        }
    }
}
