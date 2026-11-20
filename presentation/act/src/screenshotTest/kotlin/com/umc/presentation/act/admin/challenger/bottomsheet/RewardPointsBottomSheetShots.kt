package com.umc.presentation.act.admin.challenger.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

// RewardPointsBottomSheet.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = false, widthDp = 400, heightDp = 900)
@Composable
fun RewardPointsScreenShot() {
    RewardPointsScreenPreview()
}

@PreviewTest
@Preview(showBackground = false, name = "항목만 고르고 사유 없음 (등록 비활성)", widthDp = 400, heightDp = 900)
@Composable
fun RewardPointsScreenNoReasonShot() {
    RewardPointsScreenNoReasonPreview()
}

@PreviewTest
@Preview(showBackground = false, widthDp = 400, heightDp = 900)
@Composable
fun RewardPointsScreenSubmittingShot() {
    RewardPointsScreenSubmittingPreview()
}
