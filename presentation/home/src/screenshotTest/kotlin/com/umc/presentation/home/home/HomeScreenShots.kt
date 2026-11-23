package com.umc.presentation.home.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// HomeScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenShot() {
    UmcTheme { HomeScreenPreview() }
}

@PreviewTest
@Preview(showBackground = true, fontScale = 2f, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenLargeFontShot() {
    UmcTheme { HomeScreenLargeFontPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenNoGisuShot() {
    UmcTheme { HomeScreenNoGisuPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenLoadErrorShot() {
    UmcTheme { HomeScreenLoadErrorPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenLoadingShot() {
    UmcTheme { HomeScreenLoadingPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun HomeScreenListEmptyShot() {
    UmcTheme { HomeScreenListEmptyPreview() }
}
