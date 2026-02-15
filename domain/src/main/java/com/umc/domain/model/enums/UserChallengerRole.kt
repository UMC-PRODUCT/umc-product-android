package com.umc.domain.model.enums

enum class UserChallengerRole(val displayName: String?, val isVisible: Boolean) {
    SUPER_ADMIN(null, false),
    CENTRAL_PRESIDENT(null, false),
    CENTRAL_VICE_PRESIDENT(null, false),
    CENTRAL_OPERATING_TEAM_MEMBER(null, false),
    CENTRAL_EDUCATION_TEAM_MEMBER(null, false),
    CHAPTER_PRESIDENT(null, false),

    // UI에 표시할 역할들
    SCHOOL_PRESIDENT("학교 회장", true),
    SCHOOL_VICE_PRESIDENT("부회장", true),
    SCHOOL_PART_LEADER("파트장", true),

    SCHOOL_ETC_ADMIN(null, false),
    MEMBER(null, false); // 기본값

    companion object {
        fun from(role: String?): UserChallengerRole {
            return entries.find { it.name == role } ?: MEMBER
        }
    }
}