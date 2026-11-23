package com.example.mypage.memberprofile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// MemberProfileScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Composable
fun MemberProfileScreenShot() {
    MemberProfileScreenPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, heightDp = 1500)
@Composable
fun MemberProfileScreenPrivateShot() {
    MemberProfileScreenPrivatePreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
fun MemberReportSheetShot() {
    MemberReportSheetPreview()
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun MemberProfileMenuShot() {
    MemberProfileMenuPreview()
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun MemberProfileConfirmDialogShot() {
    MemberProfileConfirmDialogPreview()
}
