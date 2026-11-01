package com.umc.presentation.community.dialog

import androidx.compose.runtime.Composable
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.red100
import com.umc.component.theme.red500

/**
 * 사용자가 현재 스레드에서 나갈 때 표시하는 확인 다이얼로그
 *
 * 취소 또는 나가기 동작을 처리합니다.
 */
@Composable
fun CommunityLeaveDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    UBasicDialog(
        title = AppStrings.CHAT_LEAVE_THREAD_TITLE,
        content = AppStrings.CHAT_LEAVE_THREAD_DESCRIPTION,
        negativeText = AppStrings.COMMON_CANCEL,
        positiveText = AppStrings.CHAT_LEAVE,
        type = DialogType.ERROR,

        // 나가기 확정
        onPositive = onConfirmClick,

        // 취소 및 다이얼로그 닫기
        onNegative = onDismissRequest,
        onDismissRequest = onDismissRequest,

        showCloseButton = false,

        // 취소 버튼 스타일
        negativeBackgroundColor = grey100(),
        negativeBorderColor = grey100(),
        negativeTextColor = grey600(),

        // 나가기 버튼 스타일
        positiveBackgroundColor = red100(),
        positiveBorderColor = red100(),
        positiveTextColor = red500(),
    )
}