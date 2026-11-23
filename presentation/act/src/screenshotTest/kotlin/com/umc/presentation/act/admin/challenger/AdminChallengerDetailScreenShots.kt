package com.umc.presentation.act.admin.challenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// AdminChallengerDetailScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Preview(showBackground = true, name = "총괄단 (부여·삭제 가능)", widthDp = 400, heightDp = 900)
@Composable
fun PreviewScreenShot() {
    PreviewScreen()
}

@PreviewTest
@Preview(showBackground = true, name = "총괄단 기록 수정 모드", widthDp = 400, heightDp = 900)
@Composable
fun PreviewEditModeScreenShot() {
    PreviewEditModeScreen()
}

@PreviewTest
@Preview(showBackground = true, name = "중앙 운영국원·교내 회장단 (삭제 불가)", widthDp = 400, heightDp = 900)
@Composable
fun PreviewGrantOnlyScreenShot() {
    PreviewGrantOnlyScreen()
}

@PreviewTest
@Preview(showBackground = true, name = "Loading State", widthDp = 400, heightDp = 900)
@Composable
fun PreviewLoadingScreenShot() {
    PreviewLoadingScreen()
}

@PreviewTest
@Preview(showBackground = true, name = "Error State", widthDp = 400, heightDp = 900)
@Composable
fun PreviewErrorScreenShot() {
    PreviewErrorScreen()
}

@PreviewTest
@Preview(showBackground = false)
@Composable
fun previewDeleteDialogShot() {
    previewDeleteDialog()
}
