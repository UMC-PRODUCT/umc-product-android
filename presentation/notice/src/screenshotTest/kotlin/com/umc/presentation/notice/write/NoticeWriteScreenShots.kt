package com.umc.presentation.notice.write

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import kotlinx.collections.immutable.persistentListOf

// NoticeWriteScreen.kt 의 미리보기를 그대로 찍는다. 미리보기를 바꾸면 기준 이미지를 다시 만든다(CLAUDE.md 참고).

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteScreenShot() {
    UmcTheme { NoticeWriteScreenPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteScreenSuperAdminShot() {
    UmcTheme { NoticeWriteScreenSuperAdminPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteSingleCategoryShot() {
    UmcTheme { NoticeWriteSingleCategoryPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Preview(showBackground = true, widthDp = 600, heightDp = 900)
@Composable
fun NoticeWriteLongCategoryHintShot() {
    UmcTheme { NoticeWriteScreen(uiState = singleSchoolRoleState(
        NoticeWriterRole.SCHOOL_PART_LEADER, "아주 긴 이름을 가진 UMC 중앙대학교 캠퍼스")) }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteOtherSchoolAdminShot() {
    UmcTheme { NoticeWriteScreen(uiState = singleSchoolRoleState(NoticeWriterRole.SCHOOL_ETC_ADMIN, "UMC 대학교")) }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteSchoolCoreShot() {
    UmcTheme { NoticeWriteScreenFixedGisuPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun NoticeWriteChapterShot() {
    val category = WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU)
    UmcTheme {
        NoticeWriteScreen(uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.CHAPTER_PRESIDENT,
            selectedCategory = category, availableCategories = persistentListOf(category),
            boardChips = persistentListOf(BoardChipType.ALL, BoardChipType.PART),
            availableGisus = persistentListOf(GisuItem(12, 12)),
            authorityGisuId = 12, targetGisuId = 12, writerChapterId = 5, writerChapterName = "담당 지부",
        ))
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
fun NoticeWritePastPartCategoryShot() {
    UmcTheme {
        NoticeWriteScreen(uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.SUPER_ADMIN,
            selectedCategory = WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU),
            boardChips = persistentListOf(BoardChipType.ALL, BoardChipType.PART),
            availableGisus = persistentListOf(GisuItem(12, 12), GisuItem(9, 9)),
            authorityGisuId = 12, targetGisuId = 9, isAllSelected = true,
        ))
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
fun NoticeWritePastNoPartCategoryShot() {
    UmcTheme {
        NoticeWriteScreen(uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.SUPER_ADMIN,
            selectedCategory = WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE),
            boardChips = persistentListOf(BoardChipType.SCHOOL),
            availableGisus = persistentListOf(GisuItem(12, 12), GisuItem(9, 9)),
            authorityGisuId = 12, targetGisuId = 9,
        ))
    }
}

private fun singleSchoolRoleState(role: NoticeWriterRole, schoolName: String): NoticeWriteUiState {
    val category = if (role == NoticeWriterRole.SCHOOL_ETC_ADMIN) {
        WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE)
    } else WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER)
    val categories = persistentListOf(category)
    return NoticeWriteUiState(
        writerRole = role, availableCategories = categories, selectedCategory = selectNoticeCategory(categories, null),
        boardChips = persistentListOf(BoardChipType.PART),
        availableGisus = persistentListOf(GisuItem(12, 12)), authorityGisuId = 12, targetGisuId = 12,
        writerSchoolId = 7, writerSchoolName = schoolName,
    )
}
