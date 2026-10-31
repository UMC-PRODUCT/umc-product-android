package com.umc.domain.model.enums

/**
 * @param displayName 화면에 쓰는 한글 이름
 * @param isVisible 역할 배지로 보여줄지 여부
 * @param englishName 명함처럼 영문으로 적는 자리에 쓰는 이름
 */
enum class UserChallengerRole(val displayName: String?, val isVisible: Boolean, val englishName: String?) {
    SUPER_ADMIN("시스템 관리자", false, "System Admin"),
    CENTRAL_PRESIDENT("중앙운영사무국 총괄", false, "Central President"),
    CENTRAL_VICE_PRESIDENT("중앙운영사무국 부총괄", false, "Central Vice President"),
    CENTRAL_OPERATING_TEAM_MEMBER("중앙운영사무국 운영국", false, "Central Operating Team"),
    CENTRAL_EDUCATION_TEAM_MEMBER("중앙운영사무국 교육국", false, "Central Education Team"),
    CHAPTER_PRESIDENT("지부장", false, "Chapter President"),

    // UI에 표시할 역할들
    SCHOOL_PRESIDENT("회장", true, "School President"),
    SCHOOL_VICE_PRESIDENT("부회장", true, "School Vice President"),
    SCHOOL_PART_LEADER("파트장", true, "Part Leader"),

    SCHOOL_ETC_ADMIN("교내 운영진", false, "School Staff"),
    CHALLENGER("챌린저", false, "Challenger"),
    MEMBER(null, false, null); // 기본값

    companion object {
        fun from(role: String?): UserChallengerRole {
            return entries.find { it.name == role } ?: MEMBER
        }
    }
}