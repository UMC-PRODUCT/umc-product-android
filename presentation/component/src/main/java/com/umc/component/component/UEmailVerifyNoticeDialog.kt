package com.umc.component.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.umc.component.theme.AppStrings

/**
 * 인증 메일이 도착하지 않을 수 있다는 안내 다이얼로그
 *
 * 메일 발송이 Gmail SMTP 의 하루 발송 한도를 쓰고 있어서, 한도를 넘기면 서버는 성공으로 응답해도
 * 사용자에게 메일이 가지 않는다. 서버가 이 경우를 따로 에러로 내려주지 않아 앱에서는 구분할 방법이
 * 없으므로, 인증 요청이 접수되면 항상 이 안내를 띄운다.
 */
@Composable
fun UEmailVerifyNoticeDialog(
    onDismissRequest: () -> Unit,
) {
    UDialog(
        title = AppStrings.EMAIL_VERIFY_NOTICE_TITLE,
        content = AppStrings.EMAIL_VERIFY_NOTICE_CONTENT,
        onDismissRequest = onDismissRequest,
    )
}

@Preview(showBackground = true)
@Composable
private fun UEmailVerifyNoticeDialogPreview() {
    UEmailVerifyNoticeDialog(onDismissRequest = {})
}
