package com.umc.presentation.notice.write

import com.umc.component.theme.AppStrings
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType

internal fun toggleNoticeStaffSelection(state: NoticeWriteUiState): NoticeWriteUiState {
    if (state.isTargetLocked || BoardChipType.STAFF !in state.boardChips) return state
    return state.copy(
        isStaffSelected = !state.isStaffSelected,
        isAllSelected = false,
        // 운영진 대상과 파트 대상은 함께 발행할 수 없다.
        selectedPart = null,
    )
}

internal fun isNoticeStaffCategory(category: WriteCategoryType?): Boolean = category in setOf(
    WriteCategoryType.CENTRAL_STAFF, WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER,
)

internal fun noticeTargetGisuId(
    role: NoticeWriterRole,
    category: WriteCategoryType?,
    targetGisuId: Int?,
    authorityGisuId: Int?,
): Int? = if (role == NoticeWriterRole.CENTRAL_CORE && isNoticeStaffCategory(category)) {
    authorityGisuId
} else targetGisuId

/** 카테고리는 대상 종류만 나타내며, 기수는 별도 선택값으로 관리한다. */
internal fun createCategories(
    writerRole: NoticeWriterRole,
    gisus: List<GisuItem>,
    targetGisuId: Int?,
    canWriteAsSchoolCore: Boolean = false,
): List<WriteCategory> {
    val generation = gisus.firstOrNull { it.gisuId == targetGisuId }?.generation?.toLong()
    return createCategoriesForGeneration(writerRole, generation, canWriteAsSchoolCore)
}

/** 작성자 권한별 공지 카테고리 목록. 선택한 대상 기수의 존재 여부로 특정 기수 항목을 제한한다. */
internal fun createCategoriesForGeneration(
    writerRole: NoticeWriterRole,
    currentGeneration: Long?,
    canWriteAsSchoolCore: Boolean = false,
): List<WriteCategory> {
    return when (writerRole) {
        NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE -> buildList {
            add(WriteCategory(WriteCategoryType.ALL_GISU, AppStrings.NOTICE_WRITE_ALL_GISU))
            if (currentGeneration != null) {
                add(WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU))
            }
            add(WriteCategory(WriteCategoryType.CENTRAL_STAFF, AppStrings.NOTICE_WRITE_CATEGORY_CENTRAL))
            add(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))
            add(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
        }

        // 중앙운영진 대상(CENTRAL_MEMBER) 공지는 서버가 총괄단에게만 허용한다.
        NoticeWriterRole.CENTRAL_STAFF ->
            listOfNotNull(
                if (currentGeneration != null) WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU) else null,
                WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE),
                WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER),
            )

        // 지부장 전용 카테고리·noticeTab은 없다. 본인 지부 일반 공지는 특정 기수 카테고리로 발행한다.
        NoticeWriterRole.CHAPTER_PRESIDENT -> buildList {
            if (currentGeneration != null) {
                add(WriteCategory(WriteCategoryType.GISU, AppStrings.NOTICE_WRITE_SPECIFIC_GISU))
            }
            // 교내 회장을 겸하면 활성 기수의 학교 역할로 학교 대상 작성도 허용한다.
            if (canWriteAsSchoolCore) add(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))
        }

        NoticeWriterRole.SCHOOL_CORE ->
            listOf(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))

        NoticeWriterRole.SCHOOL_ETC_ADMIN ->
            listOf(WriteCategory(WriteCategoryType.SCHOOL_CORE, AppStrings.NOTICE_WRITE_CATEGORY_SCHOOL_CORE))

        NoticeWriterRole.SCHOOL_PART_LEADER ->
            listOf(WriteCategory(WriteCategoryType.SCHOOL_PART_LEADER, AppStrings.NOTICE_WRITE_CATEGORY_PART_LEADER))
    }
}

/**
 * (작성자 권한, 카테고리) 조합별 게시판 분류 칩 구성.
 * Super Admin·중앙 운영진·지부장·교내 운영진별 서버 작성 범위에 맞춰 분류가 달라진다.
 */
internal fun createBoardChips(
    writerRole: NoticeWriterRole,
    categoryType: WriteCategoryType,
    canWriteAsSchoolCore: Boolean = false,
): Pair<List<BoardChipType>, String?> {
    return if (writerRole == NoticeWriterRole.SUPER_ADMIN) {
        when (categoryType) {
            WriteCategoryType.SCHOOL_CORE ->
                listOf(BoardChipType.SCHOOL) to null

            // 기수 카테고리: 지부/학교/파트 (지부·학교 동시 선택 불가)
            WriteCategoryType.GISU -> listOf(
                BoardChipType.ALL, BoardChipType.CHAPTER, BoardChipType.SCHOOL, BoardChipType.PART
            ) to AppStrings.NOTICE_WRITE_GISU_CLASS_HINT

            WriteCategoryType.SCHOOL_PART_LEADER -> listOf(BoardChipType.SCHOOL) to null

            else -> emptyList<BoardChipType>() to null
        }
    } else {
        when (writerRole) {
            NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.CENTRAL_STAFF -> when (categoryType) {
                WriteCategoryType.GISU -> listOf(BoardChipType.ALL, BoardChipType.PART) to null
                // 중앙운영진 카테고리는 대상 칩 없이 CENTRAL_MEMBER로 발행한다.
                WriteCategoryType.SCHOOL_PART_LEADER -> emptyList<BoardChipType>() to null
                else -> emptyList<BoardChipType>() to null
            }

            NoticeWriterRole.CHAPTER_PRESIDENT -> when (categoryType) {
                // 지부장 대상은 본인 지부로 고정하고, 지부 전체 또는 특정 파트만 고른다.
                WriteCategoryType.GISU -> listOf(BoardChipType.ALL, BoardChipType.PART) to null
                WriteCategoryType.SCHOOL_CORE -> if (canWriteAsSchoolCore) listOf(
                    BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
                ) to null else emptyList<BoardChipType>() to null
                else -> emptyList<BoardChipType>() to null
            }

            NoticeWriterRole.SCHOOL_CORE -> listOf(
                BoardChipType.ALL, BoardChipType.STAFF, BoardChipType.PART
            ) to null

            NoticeWriterRole.SCHOOL_ETC_ADMIN -> listOf(BoardChipType.PART) to null

            // 서버는 담당 파트와 무관하게 본인 학교의 특정 파트 작성을 허용한다.
            // 학교 선택을 주지 않고 발행 요청에는 활성 기수 학교 역할의 소속 ID를 사용한다.
            NoticeWriterRole.SCHOOL_PART_LEADER -> listOf(
                BoardChipType.PART
            ) to null

            NoticeWriterRole.SUPER_ADMIN -> emptyList<BoardChipType>() to null
        }
    }
}


/** 기존 선택이 유효하면 유지하고, 그렇지 않으면 첫 카테고리를 기본 선택한다. */
internal fun selectNoticeCategory(categories: List<WriteCategory>, previous: WriteCategory?): WriteCategory? =
    categories.firstOrNull { it.type == previous?.type } ?: categories.firstOrNull()

/** Super Admin·총괄단 외에는 권한을 부여받은 기수만 작성 대상으로 허용한다. */
internal fun allowedNoticeTargetGisus(
    writerRole: NoticeWriterRole,
    gisus: List<GisuItem>,
    authorityGisuId: Int?,
    category: WriteCategoryType? = null,
): List<GisuItem> = when (writerRole) {
    NoticeWriterRole.SUPER_ADMIN -> gisus
    NoticeWriterRole.CENTRAL_CORE -> if (isNoticeStaffCategory(category)) {
        gisus.filter { it.gisuId == authorityGisuId }
    } else gisus
    else -> gisus.filter { it.gisuId == authorityGisuId }
}
