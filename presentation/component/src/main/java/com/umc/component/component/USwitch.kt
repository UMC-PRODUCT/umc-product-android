package com.umc.component.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.indigo500

/**색상 변화 통일을 위해 사용하는 Switch입니다.**/
/**
 * USwitch: 기존 Switch에서 색깔을 변경한 USwitch
 *
 * @param checked 스위치의 체크 상태
 * @param onCheckedChange 체크 상태 변경 시 호출되는 콜백
 * @param modifier 레이아웃 수정을 위한 Modifier
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
            checkedThumbColor = grey000(),
            checkedTrackColor = indigo500(),
            uncheckedThumbColor = grey300(),
            uncheckedTrackColor = grey100(),
            uncheckedBorderColor = grey300()
        )
    )
}