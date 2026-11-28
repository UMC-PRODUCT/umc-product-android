package com.umc.presentation.notice.adminnotice

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// AdminNoticeScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenShot() {
    UmcTheme { AdminNoticeScreenPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenNoAccessShot() {
    UmcTheme { AdminNoticeScreenNoAccessPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenLoadingShot() {
    UmcTheme { AdminNoticeScreenLoadingPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenErrorShot() {
    UmcTheme { AdminNoticeScreenErrorPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeChapterPresidentShot() {
    UmcTheme { AdminNoticeScreenChapterPresidentPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeAccessPendingShot() {
    UmcTheme { AdminNoticeScreenAccessPendingPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenEmptyPageRetryShot() {
    UmcTheme { AdminNoticeScreenEmptyPageRetryPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun AdminNoticeScreenPageRetryShot() {
    UmcTheme { AdminNoticeScreenPageRetryPreview() }
}
