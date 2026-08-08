package com.umc.data.response.member

import com.google.gson.annotations.SerializedName
import com.umc.data.response.member.CurrentChallengerResponse.Companion.toDomain
import com.umc.data.response.member.CurrentGisuMemberInfoResponse.Companion.toDomain
import com.umc.data.response.member.MemberPointResponse.Companion.toDomain
import com.umc.data.response.member.MemberProfileResponse.Companion.toDomain
import com.umc.data.response.member.MemberRoleResponse.Companion.toDomain
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.CurrentChallengerInfo
import com.umc.domain.model.CurrentGisuMemberInfo
import com.umc.domain.model.ProfileInfo
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
    @SerializedName("status") val status: String?,
    @SerializedName("profile") val profile: MemberProfileResponse?,

    //v2 추가 response
    @SerializedName("hasLocalCredential") val hasLocalCredential: Boolean?,
    @SerializedName("totalActivityDays") val totalActivityDays: Long?,
    @SerializedName("currentGisuMemberInfo") val currentGisuMemberInfo: CurrentGisuMemberInfoResponse?,
    @SerializedName("challengerHistory") val challengerHistory: List<ChallengerHistoryResponse>?

)
{
    companion object {
        // 1. challengerHistory ➔ 기존 challengerRecords 로 복원 매핑
        fun MemberResponse.toDomain(): UserInfo {

            val mappedProfile = profile?.toDomain() ?: ProfileInfo.empty()

            //[수정] v2의 challengerHistory -> 기존 팀원들이 쓰는 challengerRecords로 복원 매핑
            val mappedRecords: List<ChallengerRecord> = challengerHistory?.map { history ->
                with(ChallengerHistoryResponse) {
                    history.toDomain(
                        memberId = id,
                        name = name,
                        nickname = nickname,
                        email = email ?: "",
                        schoolId = schoolId,
                        schoolName = schoolName,
                        profileImageLink = profileImageLink ?: "",
                        status = status ?: "ACTIVE"
                    )
                }
            }.orEmpty()

            // [수정] v2의 history내 roleTypes -> 기존 팀원들이 쓰는 roles(List<UserRole>)로 복원 매핑
            val mappedRoles = challengerHistory?.flatMap { history ->
                history.roleTypes?.map { roleType ->
                    UserRole(
                        id = 0L,
                        challengerId = history.challengerId,
                        roleType = roleType,
                        chapterId = history.chapterId,
                        chapterName = history.chapterName,
                        organizationType = "SCHOOL",
                        organizationId = schoolId,
                        responsiblePart = history.part,
                        gisuId = history.gisuId,
                        gisu = history.generation
                    )
                }.orEmpty()
            }.orEmpty()

            return UserInfo(
                id = id,
                name = name,
                nickname = nickname,
                email = email ?: "",
                schoolId = schoolId,
                schoolName = schoolName,
                profileImageLink = profileImageLink ?: "",
                status = status ?: "ACTIVE",
                roles = mappedRoles,
                challengerRecords = mappedRecords,
                profile = mappedProfile,

                // 🌟 [v2 신규 필드 전달]
                hasLocalCredential = hasLocalCredential ?: false,
                totalActivityDays = totalActivityDays ?: 0L,
                currentGisuMemberInfo = currentGisuMemberInfo?.toDomain()
            )

        }

    }
}

// 신규 : 현재 기수 전용 DTO
data class CurrentGisuMemberInfoResponse(
    @SerializedName("gisuId") val gisuId: Long,
    @SerializedName("generation") val generation: Long,
    @SerializedName("challenger") val challenger: CurrentChallengerResponse?,
    @SerializedName("isAdmin") val isAdmin: Boolean,
    @SerializedName("roleTypes") val roleTypes: List<String>?
) {
    companion object {
        fun CurrentGisuMemberInfoResponse.toDomain(): CurrentGisuMemberInfo = CurrentGisuMemberInfo(
            gisuId = gisuId,
            generation = generation,
            challenger = challenger?.toDomain(),
            isAdmin = isAdmin,
            roleTypes = roleTypes.orEmpty()
        )
    }
}

// 신규 : 현재 기수 챌린저 상태
data class CurrentChallengerResponse(
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("part") val part: String,
    @SerializedName("challengerStatus") val challengerStatus: String,
    @SerializedName("points") val points: List<MemberPointResponse>?,
    @SerializedName("totalPoints") val totalPoints: Double?
) {
    companion object {
        fun CurrentChallengerResponse.toDomain(): CurrentChallengerInfo = CurrentChallengerInfo(
            challengerId = challengerId,
            part = part,
            challengerStatus = challengerStatus,
            points = points?.map { it.toDomain() }.orEmpty(),
            totalPoints = totalPoints ?: 0.0
        )
    }
}


data class MemberRoleResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("roleType") val roleType: String,
    @SerializedName("chapterId") val chapterId: Long?,
    @SerializedName("chapterName") val chapterName: String?,
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
            chapterId = chapterId,
            chapterName = chapterName,
            organizationType = organizationType,
            organizationId = organizationId ?: 0L,
            responsiblePart = responsiblePart,
            gisuId = gisuId,
            gisu = gisu
        )

    }
}

// [수정/대체] 기존 MemberChallengerRecordResponse -> ChallengerHistoryResponse 변경
data class ChallengerHistoryResponse(
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("gisuId") val gisuId: Long,
    @SerializedName("generation") val generation: Long, // 🔄 [수정] v1 gisu -> v2 generation
    @SerializedName("chapterId") val chapterId: Long?,
    @SerializedName("chapterName") val chapterName: String?,
    @SerializedName("part") val part: String,
    @SerializedName("challengerStatus") val challengerStatus: String?,
    @SerializedName("points") val points: List<MemberPointResponse>?,
    @SerializedName("totalPoints") val totalPoints: Double?,
    @SerializedName("roleTypes") val roleTypes: List<String>? // 🌟 [v2 신규]

    // [v1 중복 필드 제거됨]: name, nickname, email, schoolId, schoolName, profileImageLink 등
) {
    companion object {
        fun ChallengerHistoryResponse.toDomain(
            memberId: Long,
            name: String,
            nickname: String,
            email: String,
            schoolId: Long,
            schoolName: String,
            profileImageLink: String,
            status: String
        ): ChallengerRecord {
            val mappedPoints = points?.map { it.toDomain() }.orEmpty()

            val mappedRoles = roleTypes?.map { roleType ->
                UserRole(
                    id = 0L,
                    challengerId = challengerId,
                    roleType = roleType,
                    chapterId = chapterId,
                    chapterName = chapterName,
                    organizationType = "SCHOOL",
                    organizationId = schoolId,
                    responsiblePart = part,
                    gisuId = gisuId,
                    gisu = generation
                )
            }.orEmpty()

            return ChallengerRecord(
                challengerId = challengerId,
                memberId = memberId,
                gisuId = gisuId,
                gisu = generation, // 🔄 v2 generation을 기존 gisu 필드로 연결
                chapterId = chapterId,
                chapterName = chapterName,
                part = part,
                challengerStatus = challengerStatus,
                challengerPoints = mappedPoints,
                points = mappedPoints,
                totalPoint = totalPoints,
                roles = mappedRoles,

                // 🔄 루트 유저 정보에서 주입받아 호환성 유지
                name = name,
                nickname = nickname,
                email = email,
                schoolId = schoolId,
                schoolName = schoolName,
                profileImageLink = profileImageLink,
                status = status
            )
        }
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

data class MemberProfileResponse(
    @SerializedName("id") val id : Long?,
    @SerializedName("linkedIn") val linkedIn : String?,
    @SerializedName("instagram") val instagram : String?,
    @SerializedName("github") val github : String?,
    @SerializedName("blog") val blog : String?,
    @SerializedName("personal") val personal : String?,
) {
    companion object{
        fun MemberProfileResponse.toDomain(): ProfileInfo = ProfileInfo(
            id = id ?: -1L,
            linkedIn = linkedIn ?: "",
            instagram = instagram ?: "",
            github = github ?: "",
            blog = blog ?: "",
            personal = personal ?: ""
        )
    }
}
