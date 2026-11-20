package com.example.mypage.mycard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// MycardScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun CardBackEmptyShot() {
    CardBackEmptyPreview()
}

@PreviewTest
@PreviewScreenSizes
@Composable
fun MycardScreenShot() {
    MycardScreenPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MycardScreenProfileLoadFailedShot() {
    MycardScreenProfileLoadFailedPreview()
}
