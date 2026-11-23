package com.umc.presentation.study.admin.group.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// AdminStudyGroupScheduleAddScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844)
@Composable
fun AdminStudyGroupScheduleScreenShot() {
    UmcTheme { AdminStudyGroupScheduleScreenPreview() }
}
