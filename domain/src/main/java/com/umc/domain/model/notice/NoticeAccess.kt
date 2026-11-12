package com.umc.domain.model.notice

import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.UserChallengerRole

/** 공지 조회 권한 계산에는 선택 기수의 역할만 사용한다. */
fun UserInfo.noticeRolesForGisu(gisuId: Long): List<UserChallengerRole> =
    roles.asSequence()
        .filter { it.gisuId == gisuId }
        .map { UserChallengerRole.from(it.roleType) }
        .toList()
