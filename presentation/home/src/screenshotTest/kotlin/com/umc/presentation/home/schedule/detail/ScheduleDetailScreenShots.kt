package com.umc.presentation.home.schedule.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// ScheduleDetailScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PreviewScheduleDetailMenuOpenShot() {
    UmcTheme { PreviewScheduleDetailMenuOpen() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 320)
@Composable
fun PreviewScheduleDetailManyTagsShot() {
    UmcTheme { PreviewScheduleDetailManyTags() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PreviewScheduleDetailLoadingShot() {
    UmcTheme { PreviewScheduleDetailLoading() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PreviewScheduleDetailErrorShot() {
    UmcTheme { PreviewScheduleDetailError() }
}
