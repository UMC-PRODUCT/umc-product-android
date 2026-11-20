package com.umc.presentation.community.bottomsheet.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// CommunityCreateMemberBottomSheet.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetEmptyShot() {
    UmcTheme { CommunityCreateMemberSheetEmptyPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetSelectedShot() {
    UmcTheme { CommunityCreateMemberSheetSelectedPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetListUncheckedShot() {
    UmcTheme { CommunityCreateMemberSheetListUncheckedPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetSearchFocusedShot() {
    UmcTheme { CommunityCreateMemberSheetSearchFocusedPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetSearchShot() {
    UmcTheme { CommunityCreateMemberSheetSearchPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetSearchNoResultShot() {
    UmcTheme { CommunityCreateMemberSheetSearchNoResultPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetLoadingShot() {
    UmcTheme { CommunityCreateMemberSheetLoadingPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetErrorShot() {
    UmcTheme { CommunityCreateMemberSheetErrorPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityCreateMemberSheetMaxCountToastShot() {
    UmcTheme { CommunityCreateMemberSheetMaxCountToastPreview() }
}
