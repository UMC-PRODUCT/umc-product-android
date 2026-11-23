package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.organization.GisuItem

internal data class NoticeWriteSelection(
    val targetGisuId: Int?,
    val previousCategory: WriteCategory?,
    val roleChanged: Boolean,
    val resetUserSelection: Boolean,
)

/** 활성 기수의 첫 응답에서는 직접 고른 선택을 보존하고 자동 기본값만 갱신한다. */
internal fun resolveNoticeWriteSelection(
    state: NoticeWriteUiState,
    writerRole: NoticeWriterRole,
    authorityGisuId: Int?,
    knownGisus: List<GisuItem>,
    hasSelectedTargetGisu: Boolean,
    hasSelectedCategory: Boolean,
): NoticeWriteSelection {
    val roleChanged = state.writerRole != writerRole
    val authorityChanged = state.authorityGisuId != authorityGisuId
    val authorityWasKnown = state.authorityGisuId != null
    val resetUserSelection = roleChanged || (authorityChanged && authorityWasKnown)
    val resetTargetGisu = resetUserSelection || (authorityChanged && !hasSelectedTargetGisu)
    val resetCategory = resetUserSelection || (authorityChanged && !hasSelectedCategory)
    val allowedGisus = allowedNoticeTargetGisus(writerRole, knownGisus, authorityGisuId)
    val targetGisuId = state.targetGisuId.takeUnless { resetTargetGisu }
        ?.takeIf { id -> allowedGisus.any { it.gisuId == id } }
        ?: authorityGisuId
        ?: allowedGisus.firstOrNull()?.gisuId

    return NoticeWriteSelection(
        targetGisuId = noticeTargetGisuId(
            writerRole, state.selectedCategory?.type.takeUnless { resetCategory }, targetGisuId, authorityGisuId,
        ),
        previousCategory = state.selectedCategory.takeUnless { resetCategory },
        roleChanged = roleChanged,
        resetUserSelection = resetUserSelection,
    )
}
