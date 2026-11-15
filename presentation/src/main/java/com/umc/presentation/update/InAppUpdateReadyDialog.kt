package com.umc.presentation.update

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.umc.component.component.UDialog
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.grey50
import com.umc.component.theme.grey800

/**
 * 유연한 업데이트를 다 내려받았을 때 다시 시작해 적용할지 묻는다(#362).
 * 바깥을 눌러 닫으면 [onLater] 와 같다. 앱 프로세스가 살아 있는 동안은 다시 묻지 않고, 프로세스가 새로 뜨면 다시 묻는다.
 */
@Composable
internal fun InAppUpdateReadyDialog(
    onRestart: () -> Unit,
    onLater: () -> Unit,
) {
    UDialog(
        title = AppStrings.IN_APP_UPDATE_READY_TITLE,
        content = AppStrings.IN_APP_UPDATE_READY_BODY,
        onDismissRequest = onLater,
        isTwoButton = true,
        negativeText = AppStrings.IN_APP_UPDATE_LATER,
        positiveText = AppStrings.IN_APP_UPDATE_RESTART,
        onNegative = onLater,
        onPositive = onRestart,
        positiveBackgroundColor = grey800(),
        positiveTextColor = grey50(),
    )
}

@Preview(showBackground = true)
@Composable
private fun InAppUpdateReadyDialogPreview() {
    UmcTheme {
        InAppUpdateReadyDialog(onRestart = {}, onLater = {})
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun InAppUpdateReadyDialogLargeFontPreview() {
    UmcTheme {
        InAppUpdateReadyDialog(onRestart = {}, onLater = {})
    }
}
