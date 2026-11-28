package com.umc.presentation.notice.write

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.BoardChipType
import com.umc.domain.model.enums.NoticeTab
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType

/**
 * 실제 발행 요청의 수신 대상을 요약한다. 필수 선택 전에는 고정 소속 범위만 반환한다.
 * 요청이 유효하지 않거나 대상 이름을 확인할 수 없으면 null을 반환해 힌트를 숨긴다.
 */
internal fun formatNoticeRecipientHint(state: NoticeWriteUiState): String? {
    // 권한·칩 조합 검증은 요청 생성기에 맡기고, 여기서 별도의 발행 정책을 만들지 않는다.
    val request = buildNoticeTargetRequest(state) ?: return state.fixedRecipientScopeHint()
    val gisuId = request.targetGisuId ?: return AppStrings.NOTICE_WRITE_ALL_GISU_RECIPIENT
    val generation = state.generationName(gisuId) ?: return null
    // 작성자 역할이나 카테고리 이름과 달리, 실제 수신자는 요청의 noticeTab으로 결정된다.
    val recipient = when (request.targetNoticeTab) {
        NoticeTab.CHALLENGER.value -> AppStrings.ACTIVITY_HISTORY_CHALLENGER
        NoticeTab.CENTRAL_MEMBER.value -> AppStrings.NOTICE_WRITE_RECIPIENT_CENTRAL
        NoticeTab.SCHOOL_CORE.value -> AppStrings.NOTICE_WRITE_RECIPIENT_SCHOOL_CORE
        NoticeTab.SCHOOL_PART_LEADER.value -> AppStrings.NOTICE_WRITE_RECIPIENT_STAFF
        else -> return null
    }
    // 조직 ID가 있는데 이름을 못 찾은 경우 UMC 전체로 표시하면 실제 요청보다 대상이 넓어 보인다.
    val organization = when {
        request.targetSchoolId != null -> state.schoolName(request.targetSchoolId) ?: return null
        request.targetChapterId != null -> state.chapterName(request.targetChapterId) ?: return null
        request.targetNoticeTab == NoticeTab.CENTRAL_MEMBER.value -> null
        else -> AppStrings.NOTICE_WRITE_RECIPIENT_UMC
    }
    val parts = request.targetParts.map { value ->
        UserPart.entries.firstOrNull { it.serverValue == value }?.label ?: return null
    }
    return AppStrings.NOTICE_WRITE_RECIPIENT_FORMAT.format(
        (listOfNotNull(generation, organization, recipient) + parts).joinToString(" · "),
    )
}

/** 기본 boardHint에 현재 상태의 제한을 적용한다. 과거 기수 제한, 파트 필수, 기본 안내 순이다. */
internal fun resolveNoticeBoardHint(state: NoticeWriteUiState): String? = when {
    // 숨겨진 PART도 제한 안내의 근거이므로 visibleBoardChips 대신 원래 boardChips를 확인한다.
    state.targetGisuId != null && state.authorityGisuId != null &&
        state.targetGisuId > 0 && state.authorityGisuId > 0 &&
        state.targetGisuId != state.authorityGisuId &&
        state.selectedCategory?.type != WriteCategoryType.ALL_GISU && BoardChipType.PART in state.boardChips ->
        AppStrings.NOTICE_WRITE_PAST_PART_HINT
    state.writerRole == NoticeWriterRole.SCHOOL_PART_LEADER ||
        state.writerRole == NoticeWriterRole.SCHOOL_ETC_ADMIN -> AppStrings.NOTICE_WRITE_PART_REQUIRED
    else -> state.boardHint
}

private fun NoticeWriteUiState.fixedRecipientScopeHint(): String? {
    // 잘못된 선택이나 권한 오류를 확정 전 범위로 감추지 않는다.
    if (hasBoardSelection || targetGisuId != authorityGisuId || (targetGisuId ?: 0) <= 0) return null
    val category = selectedCategory?.type ?: return null
    val generation = generationName(targetGisuId) ?: return null
    val organization = when (writerRole) {
        NoticeWriterRole.CHAPTER_PRESIDENT -> when {
            category == WriteCategoryType.GISU -> chapterName(writerChapterId)
            category == WriteCategoryType.SCHOOL_CORE && canWriteAsSchoolCore -> schoolName(writerSchoolId)
            else -> null
        }
        NoticeWriterRole.SCHOOL_CORE, NoticeWriterRole.SCHOOL_ETC_ADMIN ->
            schoolName(writerSchoolId).takeIf { category == WriteCategoryType.SCHOOL_CORE }
        NoticeWriterRole.SCHOOL_PART_LEADER ->
            schoolName(writerSchoolId).takeIf { category == WriteCategoryType.SCHOOL_PART_LEADER }
        else -> null
    } ?: return null
    // 필수 칩 선택 전의 범위 안내이므로 확정 수신자를 뜻하는 '대상'은 붙이지 않는다.
    return listOf(generation, organization).joinToString(" · ")
}

private fun NoticeWriteUiState.generationName(gisuId: Int?): String? =
    availableGisus.firstOrNull { it.gisuId == gisuId && it.generation > 0 }?.let {
        AppStrings.COMMON_GENERATION_FORMAT.format(it.generation)
    }

private fun NoticeWriteUiState.schoolName(id: Int?): String? {
    if (id == null || id <= 0) return null
    // 선택 목록에 이름이 없을 때도 요청 ID와 같은 소속의 작성자 정보만 보조로 사용한다.
    return schoolList.firstOrNull { it.schoolId == id }?.schoolName?.takeIf { it.isNotBlank() }
        ?: writerSchoolName?.takeIf { id == writerSchoolId && it.isNotBlank() }
}

private fun NoticeWriteUiState.chapterName(id: Int?): String? {
    if (id == null || id <= 0) return null
    return chapterList.firstOrNull { it.id == id }?.name?.takeIf { it.isNotBlank() }
        ?: writerChapterName?.takeIf { id == writerChapterId && it.isNotBlank() }
}
