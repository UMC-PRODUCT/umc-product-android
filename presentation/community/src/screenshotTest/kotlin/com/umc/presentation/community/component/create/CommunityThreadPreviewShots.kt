package com.umc.presentation.community.component.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// CommunityThreadPreview.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 412)
@Composable
fun CommunityThreadPreviewEmptyShot() {
    UmcTheme { CommunityThreadPreviewEmptyPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412)
@Composable
fun CommunityThreadPreviewFilledShot() {
    UmcTheme { CommunityThreadPreviewFilledPreview() }
}
