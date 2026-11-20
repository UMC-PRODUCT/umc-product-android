package com.umc.presentation.act.normal.challenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// NormalChallengerScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun previewShot() {
    preview()
}

@PreviewTest
@Preview(showBackground = true, name = "Unfocused State", widthDp = 400, heightDp = 900)
@Composable
fun previewMainUnfocusedShot() {
    previewMainUnfocused()
}

@PreviewTest
@Preview(showBackground = true, name = "Loading State", widthDp = 400, heightDp = 900)
@Composable
fun previewMainLoadingShot() {
    previewMainLoading()
}

@PreviewTest
@Preview(showBackground = true, name = "Error State", widthDp = 400, heightDp = 900)
@Composable
fun previewMainErrorShot() {
    previewMainError()
}

@PreviewTest
@Preview(showBackground = true, name = "Error State (Server Message)", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainServerErrorShot() {
    PreviewMainServerError()
}
