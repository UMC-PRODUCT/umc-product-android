package com.umc.presentation.community.bottomsheet.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// CommunityMemberBottomSheet.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetCurrentMembersShot() {
    UmcTheme { CommunityMemberSheetCurrentMembersPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetEmptyShot() {
    UmcTheme { CommunityMemberSheetEmptyPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetLoadingShot() {
    UmcTheme { CommunityMemberSheetLoadingPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetSearchFocusedShot() {
    UmcTheme { CommunityMemberSheetSearchFocusedPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetSearchShot() {
    UmcTheme { CommunityMemberSheetSearchPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetSearchNoResultShot() {
    UmcTheme { CommunityMemberSheetSearchNoResultPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetUpdatingShot() {
    UmcTheme { CommunityMemberSheetUpdatingPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetErrorShot() {
    UmcTheme { CommunityMemberSheetErrorPreview() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun CommunityMemberSheetMaxCountToastShot() {
    UmcTheme { CommunityMemberSheetMaxCountToastPreview() }
}
