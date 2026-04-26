package com.umc.component.component

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral300
import com.umc.component.theme.primary500

/**색상 변화 통일을 위해 사용하는 Switch입니다.**/

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
            checkedThumbColor = neutral000(),
            checkedTrackColor = primary500(),
            uncheckedThumbColor = neutral300(),
            uncheckedTrackColor = neutral100(),
            uncheckedBorderColor = neutral300()
        )
    )
}