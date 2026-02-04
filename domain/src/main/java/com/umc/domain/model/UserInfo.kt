package com.umc.domain.model


//유저 정보 가져오는 API의 내용을 AppDataStore에 저장하기 위한 Data Class입니다.

data class UserInfo(
    val id: Int = 0,
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
    val schoolId: Int = 0,
    val schoolName: String = "",
    val profileImageLink: String = "",
    val status: String = "ACTIVE"
)