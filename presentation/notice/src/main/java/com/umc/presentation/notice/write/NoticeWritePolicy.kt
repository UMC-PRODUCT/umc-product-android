package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.WriteCategoryType

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
