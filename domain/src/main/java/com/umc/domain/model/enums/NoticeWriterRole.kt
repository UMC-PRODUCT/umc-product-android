package com.umc.domain.model.enums

/** 공지 작성자 권한. 권한에 따라 선택 가능한 카테고리와 게시판 분류가 달라짐 */
enum class NoticeWriterRole(val accessRoles: Set<UserChallengerRole>) {
    SUPER_ADMIN(setOf(UserChallengerRole.SUPER_ADMIN)),
    CENTRAL_STAFF(
        setOf(
            UserChallengerRole.CENTRAL_PRESIDENT,
            UserChallengerRole.CENTRAL_VICE_PRESIDENT,
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
        )
    ),
    // 지부장: 서버 noticeTab에 지부 등급이 없어 발행 시 지부가 유실되므로 보류.
    // 서버에 지부 등급 tab이 추가되면 주석 해제 (WriteCategoryType.CHAPTER_PRESIDENT도 함께)
    // CHAPTER_PRESIDENT(setOf(UserChallengerRole.CHAPTER_PRESIDENT)),
    SCHOOL_CORE(
        setOf(
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
            UserChallengerRole.SCHOOL_ETC_ADMIN,
        )
    ),
    SCHOOL_PART_LEADER(setOf(UserChallengerRole.SCHOOL_PART_LEADER));

    companion object {
        /** 사용자 role 중 가장 높은 작성 권한 반환. 작성 권한이 없으면 null */
        fun from(roles: List<UserChallengerRole>): NoticeWriterRole? {
            return entries.firstOrNull { writer -> roles.any { it in writer.accessRoles } }
        }
    }
}
