package com.umc.permission

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// PermissionScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Composable
fun PermissionScreenShot() {
    UmcTheme { PermissionScreenPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PermissionScreenAllCheckedShot() {
    UmcTheme { PermissionScreenAllCheckedPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PermissionScreenAlarmOnlyShot() {
    UmcTheme { PermissionScreenAlarmOnlyPreview() }
}
