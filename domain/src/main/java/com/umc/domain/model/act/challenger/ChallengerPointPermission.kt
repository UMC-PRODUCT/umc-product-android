package com.umc.domain.model.act.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.RoleType

/**
 * 챌린저 상벌점 권한. 서버 ChallengerPermissionEvaluator · ChallengerPointPermissionEvaluator 규칙을 옮겼다.
 *
 * - 상세 조회 · 부여 · 사유 수정: SUPER_ADMIN, 대상 기수의 중앙운영사무국 4역할, 대상 기수 · 대상 학교의 교내 회장 · 부회장
 * - 삭제: SUPER_ADMIN, 대상 기수의 총괄 · 부총괄
 *
 * 화면 표시용 판정이고 최종 판단은 서버가 한다.
 */
data class ChallengerPointPermission(
    val canRead: Boolean = false,
    val canGrant: Boolean = false,
    val canEditDescription: Boolean = false,
    val canDelete: Boolean = false,
) {
    /**
     * 서버가 상세 조회를 허용했을 때의 권한.
     * 서버는 조회 · 부여 · 사유 수정의 조건이 같아서, 조회가 됐다면 나머지 둘도 허용된다.
     * 앱 판정은 교내 회장단의 학교를 프로필 학교로 근사하므로 서버 결과를 우선한다.
     */
    fun withServerReadAllowed(): ChallengerPointPermission =
        copy(canRead = true, canGrant = true, canEditDescription = true)

    companion object {
        val NONE = ChallengerPointPermission()

        /**
         * @param targetGisuId 대상 챌린저의 기수 ID (generation 아님)
         * @param targetSchoolId 대상 회원의 프로필 학교 ID
         */
        fun of(
            isSuperAdmin: Boolean,
            roles: List<UserRole>,
            targetGisuId: Long?,
            targetSchoolId: Long?,
        ): ChallengerPointPermission {
            if (isSuperAdmin) return ChallengerPointPermission(true, true, true, true)
            if (targetGisuId == null || targetGisuId <= 0L) return NONE

            val rolesInGisu = roles.filter { it.gisuId == targetGisuId }
            // 중앙 역할은 organizationType 을 보지 않는다(서버도 roleType 만 본다)
            val isCentralMember = rolesInGisu.any { it.roleTypeOrNull() in CENTRAL_MEMBER_ROLES }
            val isCentralCore = rolesInGisu.any { it.roleTypeOrNull() in CENTRAL_CORE_ROLES }
            val isSchoolCore = targetSchoolId != null && targetSchoolId > 0L && rolesInGisu.any {
                it.roleTypeOrNull() in SCHOOL_CORE_ROLES &&
                    it.organizationType == ORGANIZATION_SCHOOL &&
                    it.organizationId == targetSchoolId
            }

            val canManage = isCentralMember || isSchoolCore
            return ChallengerPointPermission(
                canRead = canManage,
                canGrant = canManage,
                canEditDescription = canManage,
                canDelete = isCentralCore,
            )
        }
    }
}

fun UserInfo.challengerPointPermission(targetGisuId: Long?, targetSchoolId: Long?): ChallengerPointPermission =
    ChallengerPointPermission.of(isSuperAdmin, roles, targetGisuId, targetSchoolId)

/**
 * 운영진 챌린저 관리 탭을 쓸 수 있는지. 활성 기수에서 상세 조회 권한이 생길 수 있는 역할인지 본다.
 * 파트장 · 기타 교내 운영진 · 지부장은 목록은 받아도 상세가 403이라 여기서 막는다.
 */
fun UserInfo.canManageChallengerPoints(): Boolean {
    if (isSuperAdmin) return true
    val activeRoleTypes = currentGisuMemberInfo?.roleTypes ?: return false
    return activeRoleTypes.any { roleType ->
        roleType in CHALLENGER_MANAGE_ROLE_NAMES
    }
}

private const val ORGANIZATION_SCHOOL = "SCHOOL"

private val CENTRAL_CORE_ROLES = setOf(RoleType.CENTRAL_PRESIDENT, RoleType.CENTRAL_VICE_PRESIDENT)

private val CENTRAL_MEMBER_ROLES = CENTRAL_CORE_ROLES + setOf(
    RoleType.CENTRAL_OPERATING_TEAM_MEMBER,
    RoleType.CENTRAL_EDUCATION_TEAM_MEMBER,
)

private val SCHOOL_CORE_ROLES = setOf(RoleType.SCHOOL_PRESIDENT, RoleType.SCHOOL_VICE_PRESIDENT)

private val CHALLENGER_MANAGE_ROLE_NAMES = (CENTRAL_MEMBER_ROLES + SCHOOL_CORE_ROLES).map { it.name }.toSet()

private fun UserRole.roleTypeOrNull(): RoleType? = runCatching { RoleType.valueOf(roleType) }.getOrNull()
