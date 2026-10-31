package com.umc.component.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.umc.component.theme.white
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.main500

/**
 * 활동 화면에서 일반/운영진 모드를 전환할 때 사용하는 공용 스위치입니다.
 *
 * @param checked 현재 선택 상태
 * @param onCheckedChange 선택 상태 변경 콜백
 * @param modifier 크기와 배치를 지정하는 Modifier
 */
@Composable
fun USwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = white(),
            checkedTrackColor = main500(),
            uncheckedThumbColor = grey300(),
            uncheckedTrackColor = grey100(),
            uncheckedBorderColor = grey300()
        )
    )
}