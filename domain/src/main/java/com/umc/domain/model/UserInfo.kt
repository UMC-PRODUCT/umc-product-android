package com.umc.domain.model


//유저 정보 가져오는 API의 내용을 AppDataStore에 저장하기 위한 Data Class입니다.

data class UserInfo(
    val id: Long = 0,
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
    val schoolId: Long = 0,
    val schoolName: String = "",
    val profileImageLink: String = "",
    val status: String = "ACTIVE",
    val roles: List<UserRole> = emptyList()
)

//사용자의 권한 및 파트 정보를 담는 도메인 모델
data class UserRole(
    val id: Long,
    val challengerId: Long,
    val roleType: String,       // SUPER_ADMIN, SCHOOL_PART_LEADER 등
    val organizationType: String, // CENTRAL, SCHOOL 등
    val organizationId: Long,
    val responsiblePart: String,  // ANDROID, SPRINGBOOT 등
    val gisuId: Long
)