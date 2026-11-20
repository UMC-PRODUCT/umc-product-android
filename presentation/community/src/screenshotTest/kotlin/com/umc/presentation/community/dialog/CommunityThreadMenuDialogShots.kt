package com.umc.presentation.community.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

// CommunityThreadMenuDialog.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@PreviewScreenSizes
@Preview(name = "방장·관리자 (편집 메뉴 있음)", showBackground = true)
@Composable
fun CommunityThreadMenuDialogEditableShot() {
    UmcTheme { CommunityThreadMenuDialogEditablePreview() }
}

@PreviewTest
@Preview(name = "일반 멤버 (편집 메뉴 없음)", showBackground = true)
@Composable
fun CommunityThreadMenuDialogMemberShot() {
    UmcTheme { CommunityThreadMenuDialogMemberPreview() }
}
