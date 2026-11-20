package com.umc.presentation.community

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// CommunityScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(name = "커뮤니티 기본 목록 · Figma 13825", widthDp = 412, heightDp = 734, showBackground = true)
@Composable
fun CommunityListShot() {
    CommunityListPreview()
}

@PreviewTest
@Preview(name = "커뮤니티 빈 화면 · 낮은 높이", widthDp = 412, heightDp = 480, showBackground = true)
@Composable
fun CommunityEmptyShot() {
    CommunityEmptyPreview()
}
