package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.notice.WriteCategory
import com.umc.domain.model.notice.noticeRolesForGisu
import com.umc.domain.model.notice.noticeWriterChapterId
import com.umc.domain.model.notice.noticeWriterRole
import com.umc.domain.model.notice.noticeWriterSchoolId
import com.umc.domain.model.organization.GisuItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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

internal data class NoticeWriteTargetTransition(
    val state: NoticeWriteUiState,
    val accepted: Boolean = true,
    val resetUserSelection: Boolean = false,
)

/** 직접 선택 플래그 변경과 조직 조회는 상태 갱신 후 호출부에서 처리한다. */
internal fun applyProfileTarget(
    state: NoticeWriteUiState,
    userInfo: UserInfo,
    serverActiveGisu: GisuItem?,
    hasSelectedTargetGisu: Boolean,
    hasSelectedCategory: Boolean,
): NoticeWriteTargetTransition {
    // 서버 활성 기수가 아직 없으면 프로필의 현재 기수를 사용한다.
    val activeId = serverActiveGisu?.gisuId?.toLong() ?: userInfo.currentGisuMemberInfo?.gisuId
    val generation = serverActiveGisu?.generation?.toLong() ?: userInfo.currentGisuMemberInfo?.generation
    // roles는 전 기수 이력이므로 대상 기수와 관계없이 활성 기수의 작성 권한을 사용한다.
    val role = userInfo.noticeWriterRole(activeId ?: 0L) ?: return NoticeWriteTargetTransition(
        state.copy(writerRole = null, availableCategories = persistentListOf(),
            selectedCategory = null, boardChips = persistentListOf()),
    )
    val canWriteAsSchoolCore = userInfo.noticeRolesForGisu(activeId ?: 0L).any {
        it == UserChallengerRole.SCHOOL_PRESIDENT || it == UserChallengerRole.SCHOOL_VICE_PRESIDENT
    }
    val authorityGisu = serverActiveGisu ?: activeId?.takeIf { it > 0 }?.let { id ->
        GisuItem(id.toInt(), generation?.toInt() ?: 0, isActive = true)
    }
    val gisus = mergeTargetGisus(listOfNotNull(authorityGisu), state.availableGisus)
    val selection = resolveNoticeWriteSelection(
        state, role, authorityGisu?.gisuId, gisus, hasSelectedTargetGisu, hasSelectedCategory,
    )
    val schoolId = userInfo.noticeWriterSchoolId(activeId ?: 0L)
    val context = state.copy(
        writerRole = role,
        canWriteAsSchoolCore = canWriteAsSchoolCore,
        authorityGisuId = activeId?.toInt(),
        availableGisus = gisus.toImmutableList(),
        // 발행 대상의 소속은 활성 기수 역할의 ID를 사용한다.
        writerSchoolId = schoolId?.toInt(),
        writerSchoolName = userInfo.schoolName.takeIf { userInfo.schoolId == schoolId && it.isNotBlank() },
        writerChapterId = userInfo.noticeWriterChapterId(activeId ?: 0L)?.toInt(),
        writerChapterName = userInfo.roles.firstOrNull {
            it.gisuId == activeId && it.roleType == UserChallengerRole.CHAPTER_PRESIDENT.name
        }?.chapterName?.takeIf { it.isNotBlank() },
    )
    val next = rebuildTarget(context, selection.targetGisuId, selection.previousCategory)
    // 프로필 반영에서만 역할·겸임 변경도 칩 구성 변경으로 취급한다.
    val categoryChanged = state.selectedCategory?.type != next.selectedCategory?.type ||
        state.writerRole != next.writerRole || state.canWriteAsSchoolCore != next.canWriteAsSchoolCore
    return NoticeWriteTargetTransition(
        applyTargetSelection(
            state, next,
            refreshBoard = categoryChanged,
            resetSelection = categoryChanged || state.targetGisuId != next.targetGisuId,
        ),
        resetUserSelection = selection.resetUserSelection,
    )
}

internal fun applyLoadedGisus(
    state: NoticeWriteUiState,
    gisus: List<GisuItem>,
    preferredGisuId: Long?,
): NoticeWriteTargetTransition {
    val mergedGisus = mergeTargetGisus(gisus, state.availableGisus)
    val allowed = state.writerRole?.let {
        allowedNoticeTargetGisus(it, mergedGisus, state.authorityGisuId, state.selectedCategory?.type)
    }.orEmpty()
    val targetId = state.targetGisuId?.takeIf { id -> allowed.any { it.gisuId == id } }
        ?: preferredGisuId?.toInt()?.takeIf { id -> allowed.any { it.gisuId == id } }
        ?: allowed.firstOrNull()?.gisuId
    val next = rebuildTarget(state.copy(availableGisus = mergedGisus.toImmutableList()), targetId, state.selectedCategory)
    val categoryChanged = state.selectedCategory?.type != next.selectedCategory?.type
    return NoticeWriteTargetTransition(applyTargetSelection(
        state, next,
        refreshBoard = categoryChanged,
        resetSelection = categoryChanged || state.targetGisuId != next.targetGisuId,
    ))
}

internal fun selectTargetGisu(state: NoticeWriteUiState, gisu: GisuItem): NoticeWriteTargetTransition {
    if (state.writerRole == null || state.isTargetLocked || gisu !in state.selectableTargetGisus) {
        return NoticeWriteTargetTransition(state, accepted = false)
    }
    val next = rebuildTarget(state, gisu.gisuId, state.selectedCategory)
    return NoticeWriteTargetTransition(applyTargetSelection(
        state, next,
        refreshBoard = state.selectedCategory?.type != next.selectedCategory?.type,
        resetSelection = true,
    ))
}

internal fun selectTargetCategory(state: NoticeWriteUiState, category: WriteCategory): NoticeWriteTargetTransition {
    if (state.writerRole == null || state.isTargetLocked || category !in state.availableCategories) {
        return NoticeWriteTargetTransition(state, accepted = false)
    }
    val targetId = noticeTargetGisuId(state.writerRole, category.type, state.targetGisuId, state.authorityGisuId)
    // 명시적 카테고리 선택은 현재 목록을 재생성하지 않는다.
    val next = state.copy(selectedCategory = category, targetGisuId = targetId)
    return NoticeWriteTargetTransition(applyTargetSelection(state, next, refreshBoard = true, resetSelection = true))
}

private fun mergeTargetGisus(preferred: List<GisuItem>, existing: List<GisuItem>): List<GisuItem> =
    (preferred + existing).filter { it.gisuId > 0 && it.generation > 0 }
        .distinctBy { it.gisuId }.sortedByDescending { it.generation }

private fun rebuildTarget(
    context: NoticeWriteUiState,
    targetId: Int?,
    previousCategory: WriteCategory?,
): NoticeWriteUiState {
    val categories = context.writerRole?.let {
        createCategories(it, context.availableGisus, targetId, context.canWriteAsSchoolCore)
    }.orEmpty()
    return context.copy(
        availableCategories = categories.toImmutableList(),
        selectedCategory = selectNoticeCategory(categories, previousCategory),
        targetGisuId = targetId,
    )
}

private fun applyTargetSelection(
    before: NoticeWriteUiState,
    next: NoticeWriteUiState,
    refreshBoard: Boolean,
    resetSelection: Boolean,
): NoticeWriteUiState {
    val (chips, hint) = if (next.writerRole != null && next.selectedCategory != null) {
        createBoardChips(next.writerRole, next.selectedCategory.type, next.canWriteAsSchoolCore)
    } else emptyList<BoardChipType>() to null
    return next.copy(
        boardChips = if (refreshBoard) chips.toImmutableList() else before.boardChips,
        boardHint = if (refreshBoard) hint else before.boardHint,
        isAllSelected = if (resetSelection) false else before.isAllSelected,
        isStaffSelected = if (resetSelection) false else before.isStaffSelected,
        selectedPart = if (resetSelection) null else before.selectedPart,
        selectedChapter = if (resetSelection) null else before.selectedChapter,
        selectedSchool = if (resetSelection) null else before.selectedSchool,
    )
}
