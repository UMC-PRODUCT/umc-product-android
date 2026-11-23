package com.umc.presentation.notice.write

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
