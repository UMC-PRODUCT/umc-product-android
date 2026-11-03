package com.umc.domain.model.enums

/**
 * @param displayName 화면 문장에 쓰는 한글 이름 (명함 앞면, 활동 이력 행 문구 등)
 * @param isVisible 챌린저 목록에서 역할 배지로 보여줄지 여부
 * @param tagLabel 역할 태그(칩)에 쓰는 이름 (디자인 시스템 Tag)
 */
enum class UserChallengerRole(val displayName: String?, val isVisible: Boolean, val tagLabel: String?) {
    SUPER_ADMIN("시스템 관리자", false, "Admin"),
    CENTRAL_PRESIDENT("중앙운영사무국 총괄", false, "총괄"),
    CENTRAL_VICE_PRESIDENT("중앙운영사무국 부총괄", false, "부총괄"),
    CENTRAL_OPERATING_TEAM_MEMBER("중앙운영사무국 운영국", false, "중앙 운영진"),
    CENTRAL_EDUCATION_TEAM_MEMBER("중앙운영사무국 교육국", false, "중앙 운영진"),
    CHAPTER_PRESIDENT("지부장", false, "지부장"),

    // UI에 표시할 역할들
    SCHOOL_PRESIDENT("회장", true, "교내 회장"),
    SCHOOL_VICE_PRESIDENT("부회장", true, "교내 부회장"),
    SCHOOL_PART_LEADER("파트장", true, "교내 파트장"),

    SCHOOL_ETC_ADMIN("교내 운영진", false, "교내 운영진"),
    CHALLENGER("챌린저", false, "챌린저"),
    MEMBER(null, false, null); // 기본값

    companion object {
        fun from(role: String?): UserChallengerRole {
            return entries.find { it.name == role } ?: MEMBER
        }
    }
}
