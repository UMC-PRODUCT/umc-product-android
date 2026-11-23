package com.umc.presentation.act.normal.attendance

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// NormalAttendanceScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NormalAttendanceEmptyScreenShot() {
    NormalAttendanceEmptyScreenPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NormalAttendanceScreenShot() {
    NormalAttendanceScreenPreview()
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AvailableSessionCardCanCheckShot() {
    AvailableSessionCardCanCheckPreview()
}

@PreviewTest
@Preview(showBackground = true, name = "Available Session Expanded")
@Composable
fun AvailableSessionCardCantCheckShot() {
    AvailableSessionCardCantCheckPreview()
}

@PreviewTest
@Preview(showBackground = true, name = "Available Session Requesting")
@Composable
fun AvailableSessionCardRequestingShot() {
    AvailableSessionCardRequestingPreview()
}
