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
            id = id.toLongOrNull() ?: 0L,
            name = name,
            nickname = nickname,
            email = email,
            schoolId = schoolId.toLongOrNull() ?: 0L,
            schoolName = schoolName,
            profileImageLink = profileImageLink ?: "",
            status = status
        )
    }
}