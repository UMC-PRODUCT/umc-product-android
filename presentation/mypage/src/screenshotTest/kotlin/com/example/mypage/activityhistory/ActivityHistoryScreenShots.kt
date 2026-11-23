package com.example.mypage.activityhistory

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// ActivityHistoryScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Composable
fun ActivityHistoryScreenShot() {
    ActivityHistoryScreenPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun ActivityHistoryScreenEmptyShot() {
    ActivityHistoryScreenEmptyPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
fun ActivityHistoryScreenLoadingShot() {
    ActivityHistoryScreenLoadingPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun ActivityHistoryScreenLoadFailedShot() {
    ActivityHistoryScreenLoadFailedPreview()
}
