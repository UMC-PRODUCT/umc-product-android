package com.umc.domain.model.act.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.GisuItem

/** 선택 기수의 조직 범위. 고정 범위와 허용 목록을 벗어나는 선택은 반영하지 않는다. */
@ConsistentCopyVisibility
data class ChallengerManagementFilters private constructor(
    val gisu: GisuItem,
    val organization: ChapterWithSchool,
    val isOrganizationFixed: Boolean,
    val chapterId: Long? = null,
    val schoolId: Long? = null,
) {
    val availableSchools get() = organization.schoolList.filter { school ->
        chapterId == null || organization.schoolIdsByChapter[chapterId.toInt()].orEmpty().contains(school.schoolId)
    }.sortedBy { it.schoolName }

    fun selectChapter(id: Long?): ChallengerManagementFilters {
        if (isOrganizationFixed || chapterId == id ||
            (id != null && organization.chapterList.none { it.id.toLong() == id })) return this
        return copy(chapterId = id, schoolId = null)
    }

    fun selectSchool(id: Long?): ChallengerManagementFilters {
        if (isOrganizationFixed || (id != null && availableSchools.none { it.schoolId.toLong() == id })) return this
        return copy(schoolId = id)
    }

    companion object {
        /** null은 권한이 없거나, 회장단의 해당 기수 학교–지부 연결이 없거나 모호한 경우다. */
        fun create(user: UserInfo, gisu: GisuItem, organization: ChapterWithSchool): ChallengerManagementFilters? =
            when (user.challengerManagementAccess(gisu.gisuId.toLong())) {
                ChallengerManagementAccess.DENIED -> null
                ChallengerManagementAccess.ALL_SCHOOLS -> ChallengerManagementFilters(gisu, organization, false)
                ChallengerManagementAccess.OWN_SCHOOL -> {
                    val chapter = organization.chapterList.singleOrNull { chapter ->
                        organization.schoolIdsByChapter[chapter.id].orEmpty().any { it.toLong() == user.schoolId }
                    }
                    if (chapter == null || organization.schoolList.none { it.schoolId.toLong() == user.schoolId }) null
                    else ChallengerManagementFilters(gisu, organization, true, chapter.id.toLong(), user.schoolId)
                }
            }
    }
}
