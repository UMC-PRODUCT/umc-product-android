package com.umc.presentation.community.component.dialog

import androidx.compose.runtime.Composable
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.red100
import com.umc.component.theme.red500

@Composable
fun CommunityLeaveDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    UBasicDialog(
        title = "스레드에서 나가시겠습니까?",
        content = "대화 목록에서 사라지고 알림도 꺼져요.",
        negativeText = "취소",
        positiveText = "나가기",
        type = DialogType.ERROR,
        onPositive = onConfirmClick,
        onNegative = onDismissRequest,
        onDismissRequest = onDismissRequest,
        showCloseButton = false,

        negativeBackgroundColor = grey100(),
        negativeBorderColor = grey100(),
        negativeTextColor = grey600(),

        positiveBackgroundColor = red100(),
        positiveBorderColor = red100(),
        positiveTextColor = red500(),
    )
}