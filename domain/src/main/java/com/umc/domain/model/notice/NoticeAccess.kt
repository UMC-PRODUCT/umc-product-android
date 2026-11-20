package com.umc.domain.model.notice

import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.enums.UserChallengerRole

/** 지정된 기수에 부여된 역할만 공지 권한(조회 및 작성)에 사용한다. */
fun UserInfo.noticeRolesForGisu(gisuId: Long): List<UserChallengerRole> =
    roles.asSequence()
        .filter { it.gisuId == gisuId }
        .map { UserChallengerRole.from(it.roleType) }
        .toList()

/** 시스템 Super Admin은 기수별 챌린저 기록 없이도 작성할 수 있다. */
fun UserInfo.noticeWriterRole(gisuId: Long): NoticeWriterRole? =
    if (isSuperAdmin) NoticeWriterRole.SUPER_ADMIN
    else NoticeWriterRole.from(noticeRolesForGisu(gisuId))

/** 학교 대상 작성은 현재 회원 학교보다 활성 기수 학교 역할의 소속을 우선한다. */
fun UserInfo.noticeWriterSchoolId(authorityGisuId: Long): Long? =
    roles.firstOrNull { role ->
        role.gisuId == authorityGisuId && UserChallengerRole.from(role.roleType) in setOf(
            UserChallengerRole.SCHOOL_PRESIDENT, UserChallengerRole.SCHOOL_VICE_PRESIDENT,
            UserChallengerRole.SCHOOL_ETC_ADMIN, UserChallengerRole.SCHOOL_PART_LEADER,
        )
    }?.organizationId?.takeIf { it > 0 }
        ?: schoolId.takeIf { it > 0 }
        ?: challengerRecords.firstOrNull { it.gisuId == authorityGisuId }
            ?.schoolId?.takeIf { it > 0 }

/**
 * 지부 대상 작성에 쓰는 지부장 담당 지부 ID.
 * 서버는 CHAPTER 소속 역할의 organizationId와 대상 지부를 비교한다.
 * v2 내 정보 응답은 모든 역할을 SCHOOL 소속(organizationId = 회원 학교 ID)으로 복원하므로,
 * 이때는 organizationId 대신 역할에 실린 지부 ID를 쓴다.
 */
fun UserInfo.noticeWriterChapterId(authorityGisuId: Long): Long? {
    val role = roles.firstOrNull {
        it.gisuId == authorityGisuId &&
            UserChallengerRole.from(it.roleType) == UserChallengerRole.CHAPTER_PRESIDENT
    } ?: return null
    val chapterId = if (role.organizationType == CHAPTER_ORGANIZATION_TYPE) role.organizationId else role.chapterId
    return chapterId?.takeIf { it > 0 }
        ?: challengerRecords.firstOrNull { it.gisuId == authorityGisuId }
            ?.chapterId?.takeIf { it > 0 }
}

private const val CHAPTER_ORGANIZATION_TYPE = "CHAPTER"
