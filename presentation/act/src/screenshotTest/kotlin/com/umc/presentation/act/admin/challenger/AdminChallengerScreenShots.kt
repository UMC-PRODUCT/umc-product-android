package com.umc.presentation.act.admin.challenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// AdminChallengerScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun previewShot() {
    preview()
}

@PreviewTest
@Preview(showBackground = true, name = "Unfocused State", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainUnfocusedShot() {
    PreviewMainUnfocused()
}

@PreviewTest
@Preview(showBackground = true, name = "Loading State", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainLoadingShot() {
    PreviewMainLoading()
}

@PreviewTest
@PreviewScreenSizes
@Composable
fun PreviewLoadingMoreChallengersShot() {
    PreviewLoadingMoreChallengers()
}

@PreviewTest
@Preview(showBackground = true, name = "Error State", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainErrorShot() {
    PreviewMainError()
}

@PreviewTest
@Preview(showBackground = true, name = "권한 확인 중", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainCheckingShot() {
    PreviewMainChecking()
}

@PreviewTest
@Preview(showBackground = true, name = "권한 없음 (파트장·기타 운영진·지부장)", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainNoAccessShot() {
    PreviewMainNoAccess()
}

@PreviewTest
@Preview(showBackground = true, name = "Error State (Server Message)", widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainServerErrorShot() {
    PreviewMainServerError()
}

@PreviewTest
@Preview(showBackground = true, name = "권한별 필터 연결", widthDp = 400, heightDp = 900)
@Composable
fun PreviewConnectedSchoolFiltersShot() {
    PreviewConnectedSchoolFilters()
}
