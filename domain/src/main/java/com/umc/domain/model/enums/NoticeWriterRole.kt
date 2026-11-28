package com.umc.domain.model.enums

/**
 * 공지 작성자 권한. 역할에 따라 선택 가능한 카테고리와 발행 대상이 달라진다.
 * 시스템 Super Admin은 기수별 roles가 아닌 UserInfo.isSuperAdmin으로 판별한다.
 * 지부장 역할은 본인 지부 일반 공지를 허용한다. 교내 회장 역할을 겸하면 그 권한으로 학교 공지도 작성한다.
 */
enum class NoticeWriterRole(val accessRoles: Set<UserChallengerRole>) {
    SUPER_ADMIN(emptySet()),
    CENTRAL_CORE(setOf(UserChallengerRole.CENTRAL_PRESIDENT, UserChallengerRole.CENTRAL_VICE_PRESIDENT)),
    CENTRAL_STAFF(
        setOf(
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
        )
    ),
    // 지부장: 서버 noticeTab에 지부 등급은 없다
    // 서버는 CHALLENGER에 본인 지부의 targetChapterId와 선택적 targetParts를 담아 지부 공지를 작성하도록 허용한다.
    CHAPTER_PRESIDENT(setOf(UserChallengerRole.CHAPTER_PRESIDENT)),
    SCHOOL_CORE(
        setOf(
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
        )
    ),
    SCHOOL_ETC_ADMIN(setOf(UserChallengerRole.SCHOOL_ETC_ADMIN)),
    SCHOOL_PART_LEADER(setOf(UserChallengerRole.SCHOOL_PART_LEADER));

    companion object {
        /** 사용자 role 중 가장 높은 작성 권한 반환. 작성 권한이 없으면 null */
        fun from(roles: List<UserChallengerRole>): NoticeWriterRole? {
            return entries.firstOrNull { writer -> roles.any { it in writer.accessRoles } }
        }
    }
}
