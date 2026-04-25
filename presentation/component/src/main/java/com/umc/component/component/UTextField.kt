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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500

/**
 * UMC 공용 텍스트 입력 컴포넌트.
 *
 * 포커스 여부에 따라 테두리 색상이 자동으로 전환됨 (strokeColor ↔ focusStrokeColor).
 * disabled 상태에서는 포커스가 불가하므로 항상 strokeColor가 사용됨.
 *
 * @param value 현재 입력 텍스트
 * @param onValueChange 텍스트 변경 콜백
 * @param modifier 외부 레이아웃 지정. 너비는 modifier로 제어 (기본값은 부모 너비에 맞게 fillMaxWidth)
 * @param placeholder 입력값이 없을 때 표시하는 힌트 텍스트
 * @param placeholderColor 힌트 텍스트 색상. 기본값 neutral400
 * @param textColor 입력 텍스트 색상. 기본값 neutral800
 * @param textStyle 입력 텍스트 스타일. 기본값 Callout
 * @param backgroundColor 배경색. 기본값 neutral000 (흰색)
 * @param strokeColor 비포커스 상태의 테두리 색상. 기본값 neutral300
 * @param focusStrokeColor 포커스 상태의 테두리 색상 및 커서 색상. 기본값 primary500
 * @param cornerRadius 모서리 둥글기. 기본값 8dp
 * @param enabled false 이면 입력 불가. strokeColor는 항상 strokeColor로 고정
 * @param keyboardOptions 키보드 타입·IME 액션 등 설정
 * @param keyboardActions IME 액션(완료·검색 등) 콜백
 * @param prevIcon 입력창 왼쪽에 표시할 아이콘 (선택)
 * @param prevIconTint prevIcon 틴트 색상. null 이면 원본 색상 유지
 * @param prevIconSize prevIcon 크기. 기본값 24dp
 */
@Composable
fun UTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    placeholderColor: Color = neutral400(),
    textColor: Color = neutral800(),
    textStyle: TextStyle = UmcTypographyTokens.Callout,
    backgroundColor: Color = neutral000(),
    strokeColor: Color = neutral300(),
    focusStrokeColor: Color = primary500(),
    cornerRadius: Dp = 8.dp,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    prevIcon: Painter? = null,
    prevIconTint: Color? = null,
    prevIconSize: Dp = 24.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val currentStrokeColor = if (isFocused && enabled) focusStrokeColor else strokeColor
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
        modifier = modifier,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape)
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
                        Text(
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
