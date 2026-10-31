package com.umc.domain.model.enums

enum class UserChallengerRole(val displayName: String?, val isVisible: Boolean) {
    SUPER_ADMIN("시스템 관리자", false),
    CENTRAL_PRESIDENT("중앙운영사무국 총괄", false),
    CENTRAL_VICE_PRESIDENT("중앙운영사무국 부총괄", false),
    CENTRAL_OPERATING_TEAM_MEMBER("중앙운영사무국 운영국", false),
    CENTRAL_EDUCATION_TEAM_MEMBER("중앙운영사무국 교육국", false),
    CHAPTER_PRESIDENT("지부장", false),

    // UI에 표시할 역할들
    SCHOOL_PRESIDENT("회장", true),
    SCHOOL_VICE_PRESIDENT("부회장", true),
    SCHOOL_PART_LEADER("파트장", true),

    SCHOOL_ETC_ADMIN("교내 운영진", false),
    CHALLENGER("챌린저", false),
    MEMBER(null, false); // 기본값

    companion object {
        fun from(role: String?): UserChallengerRole {
            return entries.find { it.name == role } ?: MEMBER
        }
    }
}