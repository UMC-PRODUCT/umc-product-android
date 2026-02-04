package com.umc.data.response.member

import com.umc.domain.model.UserInfo

data class MemberResponse(
    val id: String,
    val name: String,
    val nickname: String,
    val email: String,
    val schoolId: String,
    val schoolName: String,
    val profileImageLink: String?,
    val status: String
) {
    companion object {
        fun MemberResponse.toDomain(): UserInfo = UserInfo(
            id = id.toIntOrNull() ?: 0,
            name = name,
            nickname = nickname,
            email = email,
            schoolId = schoolId.toIntOrNull() ?: 0,
            schoolName = schoolName,
            profileImageLink = profileImageLink ?: "",
            status = status
        )
    }
}