package com.umc.presentation.notice.write

import com.umc.domain.model.enums.NoticeTab
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WriteCategoryType
import com.umc.domain.model.request.notice.NoticeTargetRequest

/**
 * 화면 선택을 서버가 허용하는 공지 대상 조합으로 변환한다. null이면 등록할 수 없다.
 * noticeTab은 CHALLENGER(일반), CENTRAL_MEMBER(중앙 운영진), SCHOOL_CORE(학교 회장단), SCHOOL_PART_LEADER(학교 파트장)를 사용한다.
 * 운영진 공지 칩은 파트가 있으면 SCHOOL_PART_LEADER, 없으면 SCHOOL_CORE로 발행하며, Super Admin의 중앙 운영진 카테고리만 CENTRAL_MEMBER로 발행한다.
 * 작성 권한에 없는 대상이나 소속 ID가 누락된 조합은 서버 요청 전에 거절한다.
 */
internal fun buildNoticeTargetRequest(state: NoticeWriteUiState): NoticeTargetRequest? {
    val category = state.selectedCategory?.type ?: return null
    val writerRole = state.writerRole ?: return null
    // 지부장과 교내 회장 역할을 함께 가진 경우 카테고리별 권한으로 요청을 만든다.
    val role = if (writerRole == NoticeWriterRole.CHAPTER_PRESIDENT &&
        category == WriteCategoryType.SCHOOL_CORE && state.canWriteAsSchoolCore
    ) NoticeWriterRole.SCHOOL_CORE else writerRole

    if (state.selectedPart != null && state.selectedPart !in UserPart.selectable) return null
    if (state.selectedChapter != null && state.chapterList.none { it.id == state.selectedChapter.id }) return null
    if (state.selectedSchool != null && state.schoolList.none { it.schoolId == state.selectedSchool.schoolId }) return null
    val part = state.selectedPart?.name
    val targetSchoolId = state.selectedSchool?.schoolId?.takeIf { it > 0 }
    if (state.selectedSchool != null && targetSchoolId == null) return null

    if (category == WriteCategoryType.ALL_GISU) {
        // 전체 기수 공지에는 기수와 조직 필터를 함께 지정할 수 없다.
        if (role != NoticeWriterRole.SUPER_ADMIN && role != NoticeWriterRole.CENTRAL_CORE) return null
        if (state.selectedChapter != null || state.selectedSchool != null || part != null ||
            state.isStaffSelected || state.isAllSelected) return null
        return NoticeTargetRequest(null, null, null, emptyList(), NoticeTab.CHALLENGER.value)
    }

    val gisuId = state.targetGisuId?.takeIf { it > 0 } ?: return null
    if (state.availableGisus.none { it.gisuId == gisuId }) return null
    // 일반 운영진은 권한을 부여받은 기수에만 발행할 수 있다.
    if (role != NoticeWriterRole.SUPER_ADMIN && role != NoticeWriterRole.CENTRAL_CORE &&
        gisuId != state.authorityGisuId) return null

    return when (role) {
        NoticeWriterRole.SUPER_ADMIN, NoticeWriterRole.CENTRAL_CORE, NoticeWriterRole.CENTRAL_STAFF ->
            state.centralTarget(role, category, gisuId, part, targetSchoolId)
        NoticeWriterRole.CHAPTER_PRESIDENT -> state.chapterTarget(category, gisuId, part)
        else -> state.schoolTarget(role, category, gisuId, part)
    }
}

/** 중앙 권한의 카테고리별 대상을 만든다. 지부·학교 지정은 Super Admin의 기수 공지에만 허용한다. */
private fun NoticeWriteUiState.centralTarget(
    role: NoticeWriterRole,
    category: WriteCategoryType,
    gisuId: Int,
    part: String?,
    targetSchoolId: Int?,
): NoticeTargetRequest? {
    return when (category) {
        WriteCategoryType.GISU -> {
            if (!isAllSelected && part == null && selectedChapter == null && selectedSchool == null) return null
            if (role != NoticeWriterRole.SUPER_ADMIN && (selectedChapter != null || selectedSchool != null)) return null
            if (selectedChapter != null && selectedSchool != null) return null
            NoticeTargetRequest(
                targetGisuId = gisuId,
                targetChapterId = selectedChapter?.id?.toInt().takeUnless { isAllSelected },
                targetSchoolId = targetSchoolId.takeUnless { isAllSelected },
                targetParts = if (isAllSelected) emptyList() else listOfNotNull(part),
                targetNoticeTab = NoticeTab.CHALLENGER.value,
            )
        }
        WriteCategoryType.CENTRAL_STAFF -> centralStaffTarget(role, gisuId, part)
        WriteCategoryType.SCHOOL_CORE, WriteCategoryType.SCHOOL_PART_LEADER -> {
            if (role != NoticeWriterRole.SUPER_ADMIN && targetSchoolId != null) return null
            val tab = if (category == WriteCategoryType.SCHOOL_CORE) NoticeTab.SCHOOL_CORE else NoticeTab.SCHOOL_PART_LEADER
            val parts = if (category == WriteCategoryType.SCHOOL_CORE) emptyList() else listOfNotNull(part)
            NoticeTargetRequest(gisuId, null, targetSchoolId, parts, tab.value)
        }
        else -> null
    }
}

/** 중앙 운영진 카테고리를 변환한다. Super Admin의 요청만 CENTRAL_MEMBER 탭으로 발행한다. */
private fun NoticeWriteUiState.centralStaffTarget(
    role: NoticeWriterRole,
    gisuId: Int,
    part: String?,
): NoticeTargetRequest? {
    if (selectedChapter != null || selectedSchool != null) return null
    if (role == NoticeWriterRole.SUPER_ADMIN) {
        if (isAllSelected || isStaffSelected || part != null) return null
        return NoticeTargetRequest(gisuId, null, null, emptyList(), NoticeTab.CENTRAL_MEMBER.value)
    }
    if (isAllSelected && (isStaffSelected || part != null)) return null
    if (isStaffSelected) {
        // 중앙 권한의 운영진 공지는 파트 유무에 따라 회장단·파트장 탭으로 발행한다.
        val tab = if (part == null) NoticeTab.SCHOOL_CORE else NoticeTab.SCHOOL_PART_LEADER
        return NoticeTargetRequest(gisuId, null, null, listOfNotNull(part), tab.value)
    }
    if (!isAllSelected && part == null) return null
    return NoticeTargetRequest(gisuId, null, null, listOfNotNull(part), NoticeTab.CHALLENGER.value)
}

/** 지부장 전용 탭이 없어 본인 지부 ID를 지정한 CHALLENGER 공지로 발행한다. */
private fun NoticeWriteUiState.chapterTarget(
    category: WriteCategoryType,
    gisuId: Int,
    part: String?,
): NoticeTargetRequest? {
    val chapterId = writerChapterId?.takeIf { it > 0 } ?: return null
    if (category != WriteCategoryType.GISU || isStaffSelected ||
        selectedSchool != null || selectedChapter != null) return null
    if (!isAllSelected && part == null) return null
    return NoticeTargetRequest(
        gisuId, chapterId, null,
        if (isAllSelected) emptyList() else listOfNotNull(part),
        NoticeTab.CHALLENGER.value,
    )
}

/** 교내 작성 권한에 맞는 카테고리와 탭을 선택한다. 학교 ID가 없으면 대상이 넓어질 수 있어 거절한다. */
private fun NoticeWriteUiState.schoolTarget(
    role: NoticeWriterRole,
    category: WriteCategoryType,
    gisuId: Int,
    part: String?,
): NoticeTargetRequest? {
    val schoolId = writerSchoolId?.takeIf { it > 0 } ?: return null
    val allowedCategory = when (role) {
        NoticeWriterRole.SCHOOL_CORE, NoticeWriterRole.SCHOOL_ETC_ADMIN -> WriteCategoryType.SCHOOL_CORE
        NoticeWriterRole.SCHOOL_PART_LEADER -> WriteCategoryType.SCHOOL_PART_LEADER
        else -> return null
    }
    if (category != allowedCategory || selectedChapter != null || selectedSchool != null) return null
    if ((role == NoticeWriterRole.SCHOOL_PART_LEADER || role == NoticeWriterRole.SCHOOL_ETC_ADMIN) && part == null) return null

    if (isStaffSelected) {
        if (role != NoticeWriterRole.SCHOOL_CORE) return null
        val tab = if (part == null) NoticeTab.SCHOOL_CORE else NoticeTab.SCHOOL_PART_LEADER
        return NoticeTargetRequest(gisuId, null, schoolId, listOfNotNull(part), tab.value)
    }
    if (!isAllSelected && part == null) return null
    // 학교 공지의 '전체'는 본인 학교 전체를 뜻하므로 schoolId를 유지한다.
    return NoticeTargetRequest(
        gisuId, null, schoolId,
        if (isAllSelected) emptyList() else listOfNotNull(part),
        NoticeTab.CHALLENGER.value,
    )
}
