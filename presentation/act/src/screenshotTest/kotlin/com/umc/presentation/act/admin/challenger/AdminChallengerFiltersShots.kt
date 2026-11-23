package com.umc.presentation.act.admin.challenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.android.tools.screenshot.PreviewTest

// AdminChallengerFilters.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 412, name = "필터 선택형")
@PreviewScreenSizes
@Composable
fun ChallengerSelectableFiltersShot() {
    ChallengerSelectableFiltersPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, name = "필터 고정형")
@Composable
fun ChallengerFixedFiltersShot() {
    ChallengerFixedFiltersPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, heightDp = 560, name = "학교 선택")
@Composable
fun ChallengerSchoolFilterShot() {
    ChallengerSchoolFilterPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, heightDp = 560, name = "학교 검색 결과 없음")
@Composable
fun ChallengerSchoolFilterEmptyShot() {
    ChallengerSchoolFilterEmptyPreview()
}

@PreviewTest
@Preview(showBackground = true, widthDp = 412, heightDp = 840, name = "학교 선택 긴 목록")
@Composable
fun ChallengerLongSchoolFilterShot() {
    ChallengerLongSchoolFilterPreview()
}
