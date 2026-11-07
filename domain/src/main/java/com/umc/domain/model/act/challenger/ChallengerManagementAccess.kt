package com.umc.domain.model.act.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.organization.GisuItem

enum class ChallengerManagementAccess { DENIED, ALL_SCHOOLS, OWN_SCHOOL }

/**
 * 선택 기수의 역할과 현재 프로필 학교로 UI 검색 범위를 미리 판단한다.
 * v2 프로필의 과거 역할 organizationId는 현재 학교로 채워질 수 있어 최종 권한은 검색 API가 검사한다.
 */
fun UserInfo.challengerManagementAccess(gisuId: Long): ChallengerManagementAccess {
    if (gisuId <= 0) return ChallengerManagementAccess.DENIED
    if (isSuperAdmin) return ChallengerManagementAccess.ALL_SCHOOLS
    val rolesInGisu = roles.filter { it.gisuId == gisuId }
    if (rolesInGisu.any { it.roleType in CENTRAL_SEARCH_ROLES }) {
        return ChallengerManagementAccess.ALL_SCHOOLS
    }
    return if (schoolId > 0 && rolesInGisu.any {
        it.roleType in SCHOOL_SEARCH_ROLES && it.organizationType == "SCHOOL" && it.organizationId == schoolId
    }) ChallengerManagementAccess.OWN_SCHOOL else ChallengerManagementAccess.DENIED
}

/** 최고 관리자는 전체 기수 목록을 사용하며, 일반 운영진은 해당 기수 검색 역할이 있는 항목만 사용한다. */
fun UserInfo.searchableChallengerGisus(gisus: List<GisuItem>): List<GisuItem> = gisus
    .filter { it.gisuId > 0 && it.generation > 0 && challengerManagementAccess(it.gisuId.toLong()) != ChallengerManagementAccess.DENIED }
    .distinctBy { it.gisuId }
    .sortedByDescending { it.generation }

/** 재진입 시 유효한 선택을 보존하고, 최초 진입은 활성 기수 또는 generation이 가장 높은 허용 기수를 고른다. */
fun List<GisuItem>.initialChallengerGisu(preferredGisuId: Long? = null): GisuItem? =
    firstOrNull { it.gisuId.toLong() == preferredGisuId }
        ?: firstOrNull { it.isActive }
        ?: maxByOrNull { it.generation }

private val CENTRAL_SEARCH_ROLES = setOf(
    "CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT", "CENTRAL_OPERATING_TEAM_MEMBER", "CENTRAL_EDUCATION_TEAM_MEMBER",
)
private val SCHOOL_SEARCH_ROLES = setOf("SCHOOL_PRESIDENT", "SCHOOL_VICE_PRESIDENT")
