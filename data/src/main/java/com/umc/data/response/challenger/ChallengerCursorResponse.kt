package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName
import com.umc.data.response.base.CursorResponse
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.model.act.challenger.UserPartCount
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.ParticipantSearchPage

/**
 * 챌린저 목록 조회를 위한 커서 기반 응답 DTO
 */
data class ChallengerCursorResponse(
    @SerializedName("cursor")
    val cursor: CursorResponse<ChallengerCursorItemResponse>,
    @SerializedName("partCounts")
    val partCounts: List<ChallengerPartCountResponse>
) {
    fun toModel(): ChallengerList {
        return ChallengerList(
            challengers = cursor.content.map { it.toModel() },
            partCounts = partCounts.map { it.toModel() },
            nextCursor = cursor.nextCursor,
            hasNext = cursor.hasNext
        )
    }

    fun toAdminList(): AdminChallengerList {
        return AdminChallengerList(
            challengers = cursor.content.map { it.toAdminModel() },
            nextCursor = cursor.nextCursor,
            hasNext = cursor.hasNext
        )
    }

    fun toParticipantSearchPage(): ParticipantSearchPage {
        return ParticipantSearchPage(
            // cursor 내부의 content 리스트를 순회하며 ParticipantItem으로 변환합니다.
            content = this.cursor.content.map { it.toParticipantItem() },
            nextCursor = this.cursor.nextCursor,
            hasNext = this.cursor.hasNext
        )
    }

}

/**
 * 커서 기반 리스트에 포함되는 개별 챌린저 아이템 DTO
 */
data class ChallengerCursorItemResponse(
    @SerializedName("challengerId")
    val challengerId: Long,
    @SerializedName("memberId")
    val memberId: Long,
    @SerializedName("gisuId")
    val gisuId: Long,
    @SerializedName("gisu")
    val gisu: Int,
    @SerializedName("part")
    val part: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("schoolName")
    val schoolName: String,
    @SerializedName("pointSum")
    val pointSum: Double,
    @SerializedName("profileImageLink")
    val profileImageLink: String?,
    @SerializedName("roleTypes")
    val roleTypes: List<String>
) {
    fun toModel(): UserChallenger {
        return UserChallenger(
            id = challengerId,
            name = name,
            nickname = nickname,
            generation = gisu,
            part = UserPart.valueOf(part),
            role = extractDisplayRole(roleTypes),
            pointSum = if (pointSum < 0) 0.0 else pointSum,
            profileImage = profileImageLink,
            schoolName = schoolName,
        )
    }

    fun toAdminModel(): AdminChallenger {
        return AdminChallenger(
            id = challengerId.toInt(),
            name = name,
            nickname = nickname,
            generation = gisu,
            part = UserPart.valueOf(part),
            outCount = 0, // 초기값 설정
            warningCount = 0, // 초기값 설정
            profileImage = profileImageLink
        )
    }
    
    //일정 생성용 변경
    fun toParticipantItem(): ParticipantItem{
        return ParticipantItem(
            id = memberId,
            name = name,
            nickname = nickname,
            school = schoolName,
            gisu = gisu.toLong(),
            userPart = UserPart.valueOf(part),
            profileImage = profileImageLink ?: ""
            
        )
    }

    private fun extractDisplayRole(roles: List<String>): UserChallengerRole {
        return when {
            roles.contains("SCHOOL_PRESIDENT") -> UserChallengerRole.SCHOOL_PRESIDENT
            roles.contains("SCHOOL_VICE_PRESIDENT") -> UserChallengerRole.SCHOOL_VICE_PRESIDENT
            roles.contains("SCHOOL_PART_LEADER") -> UserChallengerRole.SCHOOL_PART_LEADER
            else -> UserChallengerRole.MEMBER
        }
    }
}

/**
 * 파트별 인원 수 응답 DTO
 */
data class ChallengerPartCountResponse( // 명확성을 위해 이름 변경
    @SerializedName("part")
    val part: String,
    @SerializedName("count")
    val count: Int
) {
    fun toModel(): UserPartCount = UserPartCount(
        part = UserPart.valueOf(part),
        count = count
    )
}