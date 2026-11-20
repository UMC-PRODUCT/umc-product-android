package com.example.mypage.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// ProfileScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 1400)
@Composable
fun ProfileScreenShot() {
    ProfileScreenPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ProfileScreenLoadFailedShot() {
    ProfileScreenLoadFailedPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun ProfileTopbarSavingShot() {
    ProfileTopbarSavingPreview()
}
