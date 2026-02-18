package com.umc.data.response.member

import com.google.gson.annotations.SerializedName
import com.umc.data.response.member.MemberChallengerRecordResponse.Companion.toDomain
import com.umc.data.response.member.MemberPointResponse.Companion.toDomain
import com.umc.data.response.member.MemberRoleResponse.Companion.toDomain
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.enums.PointType

data class MemberResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("email") val email: String?,
    @SerializedName("schoolId") val schoolId: Long,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("profileImageLink") val profileImageLink: String?,
    @SerializedName("status") val status: String,
    @SerializedName("roles") val roles: List<MemberRoleResponse>?,
    @SerializedName("challengerRecords") val challengerRecords: List<MemberChallengerRecordResponse>?
)
{
    companion object {
        fun MemberResponse.toDomain(): UserInfo = UserInfo(
            id = id,
            name = name,
            nickname = nickname,
            email = email ?: "",
            schoolId = schoolId,
            schoolName = schoolName,
            profileImageLink = profileImageLink ?: "",
            status = status,
            roles = roles?.map { it.toDomain() }.orEmpty(),
            challengerRecords = challengerRecords?.map { it.toDomain() }.orEmpty()
        )
    }
}


data class MemberRoleResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("roleType") val roleType: String,
    @SerializedName("organizationType") val organizationType: String,
    @SerializedName("organizationId") val organizationId: Long?,
    @SerializedName("responsiblePart") val responsiblePart: String,
    @SerializedName("gisuId") val gisuId: Long,
    @SerializedName("gisu") val gisu: Long
) {
    companion object{
        fun MemberRoleResponse.toDomain(): UserRole = UserRole(
            id = id,
            challengerId = challengerId,
            roleType = roleType,
            organizationType = organizationType,
            organizationId = organizationId ?: 0L,
            responsiblePart = responsiblePart,
            gisuId = gisuId,
            gisu = gisu
        )

    }
}

data class MemberChallengerRecordResponse(
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("gisuId") val gisuId: Long,
    @SerializedName("gisu") val gisu: Long,
    @SerializedName("chapterId") val chapterId: Long?,
    @SerializedName("chapterName") val chapterName: String?,
    @SerializedName("part") val part: String,
    @SerializedName("challengerPoints") val challengerPoints: List<MemberPointResponse>?,
    @SerializedName("challengerStatus") val challengerStatus: String?,
    @SerializedName("name") val name: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("email") val email: String?,
    @SerializedName("schoolId") val schoolId: Long,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("profileImageLink") val profileImageLink: String?,
    @SerializedName("status") val status: String
) {
    companion object{
        fun MemberChallengerRecordResponse.toDomain(): ChallengerRecord = ChallengerRecord(
            challengerId = challengerId,
            memberId = memberId,
            gisuId = gisuId,
            gisu = gisu,
            chapterId = chapterId,
            chapterName = chapterName,
            part = part,
            challengerStatus = challengerStatus,
            challengerPoints = challengerPoints?.map { it.toDomain() }.orEmpty(),
            name = name,
            nickname = nickname,
            email = email ?: "",
            schoolId = schoolId,
            schoolName = schoolName,
            profileImageLink = profileImageLink ?: "",
            status = status

        )
    }
}

data class MemberPointResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("pointType") val pointType: String,
    @SerializedName("point") val point: Double,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String
) {
    companion object {
        fun MemberPointResponse.toDomain(): ChallengerPoint = ChallengerPoint(
            id = id,
            title = description,
            pointType = runCatching { PointType.valueOf(pointType.uppercase()) }.
                getOrDefault(PointType.WARNING),
            value = point,
            date = createdAt
        )
    }
}
