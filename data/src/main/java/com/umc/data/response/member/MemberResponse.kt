package com.umc.data.response.member

import com.google.gson.annotations.SerializedName
import com.umc.data.response.member.MemberRoleResponse.Companion.toDomain
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole

data class MemberResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("email") val email: String,
    @SerializedName("schoolId") val schoolId: Long,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("profileImageLink") val profileImageLink: String?,
    @SerializedName("status") val status: String,
    @SerializedName("roles") val roles: List<MemberRoleResponse>
) {
    companion object {
        fun MemberResponse.toDomain(): UserInfo = UserInfo(
            id = id,
            name = name,
            nickname = nickname,
            email = email,
            schoolId = schoolId,
            schoolName = schoolName,
            profileImageLink = profileImageLink ?: "",
            status = status,
            roles = roles.map { it.toDomain() }
        )
    }
}


data class MemberRoleResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("roleType") val roleType: String,
    @SerializedName("organizationType") val organizationType: String,
    @SerializedName("organizationId") val organizationId: Long,
    @SerializedName("responsiblePart") val responsiblePart: String,
    @SerializedName("gisuId") val gisuId: Long
) {
    companion object{
        fun MemberRoleResponse.toDomain(): UserRole = UserRole(
            id = id,
            challengerId = challengerId,
            roleType = roleType,
            organizationType = organizationType,
            organizationId = organizationId,
            responsiblePart = responsiblePart,
            gisuId = gisuId
        )

    }
}