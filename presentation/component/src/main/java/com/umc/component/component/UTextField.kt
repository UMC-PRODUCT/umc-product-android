package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo500

/**
 * 검색어, 사유, 점수 설명 입력에 사용하는 공용 텍스트 필드입니다.
 *
 * 포커스 여부에 따라 배경과 테두리 색상을 전환하며, 활동 화면에서 사용하는 선행 아이콘과
 * 키보드 액션을 지원합니다.
 *
 * @param value 현재 입력값
 * @param onValueChange 입력값 변경 콜백
 * @param modifier 크기와 배치를 지정하는 Modifier
 * @param placeholder 값이 비어 있을 때 표시할 안내 문구
 * @param placeholderColor 안내 문구 색상
 * @param textColor 입력 텍스트 색상
 * @param textStyle 입력 텍스트 스타일
 * @param backgroundColor 기본 배경색
 * @param strokeColor 기본 테두리 색상
 * @param focusStrokeColor 포커스 상태의 테두리와 커서 색상
 * @param cornerRadius 모서리 반경
 * @param enabled 입력 가능 여부
 * @param focusBackgroundColor 포커스 상태의 배경색
 * @param keyboardOptions 키보드 옵션
 * @param keyboardActions 키보드 액션
 * @param prevIcon 입력창 앞에 표시할 아이콘
 * @param prevIconTint 앞 아이콘 색상. null이면 원본 색상을 유지합니다.
 * @param prevIconSize 앞 아이콘 크기
 * @param onFocusChange 포커스 상태 변경 콜백
 */
@Composable
fun UTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    placeholderColor: Color = grey400(),
    textColor: Color = grey800(),
    textStyle: TextStyle = UmcTypographyTokens.Callout,
    backgroundColor: Color = grey000(),
    strokeColor: Color = grey300(),
    focusStrokeColor: Color = indigo500(),
    cornerRadius: Dp = 8.dp,
    enabled: Boolean = true,
    focusBackgroundColor: Color = backgroundColor,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    prevIcon: Painter? = null,
    prevIconTint: Color? = null,
    prevIconSize: Dp = 24.dp,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val currentStrokeColor = if (isFocused && enabled) focusStrokeColor else strokeColor

    val currentBackgroundColor = if (isFocused && enabled) {
        focusBackgroundColor
    } else {
        backgroundColor
    }

    val shape = RoundedCornerShape(cornerRadius)

    // OutlinedTextField 대신 BasicTextField를 사용해 커스텀 decoration box 적용.
    // 내부 패딩(16dp/14dp)과 배경·테두리를 decoration box 안에서 직접 제어함
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = textStyle.copy(color = textColor),
        cursorBrush = SolidColor(focusStrokeColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        modifier = modifier.onFocusChanged { onFocusChange?.invoke(it.isFocused) },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentBackgroundColor, shape)
                    .border(1.dp, currentStrokeColor, shape)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prevIcon != null) {
                    Icon(
                        painter = prevIcon,
                        contentDescription = null,
                        tint = prevIconTint ?: Color.Unspecified,
                        modifier = Modifier.size(prevIconSize),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // placeholder와 실제 커서(innerTextField)를 같은 위치에 겹쳐 렌더링.
                // BasicTextField는 hint를 자체 지원하지 않아 수동으로 처리
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        UText(
                            text = placeholder,
                            style = textStyle.copy(color = placeholderColor),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}
