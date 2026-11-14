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
