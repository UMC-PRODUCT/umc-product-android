package com.umc.presentation.community.chatting

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// CommunityChattingScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CommunityChattingScreenShot() {
    UmcTheme { CommunityChattingScreenPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CommunityChattingScreenViewOnlyShot() {
    UmcTheme { CommunityChattingScreenViewOnlyPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CommunityChattingScreenViewOnlyEmptyShot() {
    UmcTheme { CommunityChattingScreenViewOnlyEmptyPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CommunityChattingScreenNoAccessShot() {
    UmcTheme { CommunityChattingScreenNoAccessPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun CommunityChattingScreenKickedShot() {
    UmcTheme { CommunityChattingScreenKickedPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 320)
@Composable
fun SourceNavigationStatusShot() {
    UmcTheme { SourceNavigationStatusPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CommunityChattingEditFailureShot() {
    UmcTheme { CommunityChattingEditFailurePreview() }
}
