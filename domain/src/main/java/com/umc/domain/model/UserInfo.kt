package com.umc.domain.model

import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.mypage.UserCard


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
    val roles: List<UserRole> = emptyList(),
    val challengerRecords: List<ChallengerRecord> = emptyList(),
    val profile: ProfileInfo = ProfileInfo(0, "", "", "", "", ""),

    val hasLocalCredential: Boolean = false,
    val totalActivityDays: Long = 0L,
    val currentGisuMemberInfo: CurrentGisuMemberInfo? = null
)


//v2 신규: 현재 활성 기수 정보
data class CurrentGisuMemberInfo(
    val gisuId: Long,
    val generation: Long,
    val challenger: CurrentChallengerInfo?,
    val isAdmin: Boolean,
    val roleTypes: List<String>
)

//v2 신규: 현재 활성 기수의 챌린저 상세 정보
data class CurrentChallengerInfo(
    val challengerId: Long,
    val part: String,
    val challengerStatus: String,
    val points: List<ChallengerPoint> = emptyList(),
    val totalPoints: Double = 0.0
)

//사용자의 권한 및 파트 정보를 담는 도메인 모델
data class UserRole(
    val id: Long,
    val challengerId: Long,
    val roleType: String,       // SUPER_ADMIN, SCHOOL_PART_LEADER 등
    val chapterId: Long?,
    val chapterName: String?,
    val organizationType: String, // CENTRAL, SCHOOL 등
    val organizationId: Long?,
    val responsiblePart: String?,  // ANDROID, SPRINGBOOT 등
    val gisuId: Long,
    val gisu: Long,
)

//현재 챌린저의 기록을 담는 도메인 모델
data class ChallengerRecord(
    val challengerId: Long,
    val memberId: Long,
    val gisuId: Long,
    val gisu: Long,
    val chapterId: Long?,
    val chapterName: String?,
    val part: String,
    val challengerStatus: String?,
    val challengerPoints: List<ChallengerPoint> = emptyList(),
    val points: List<ChallengerPoint> = emptyList(), //추기
    val totalPoint: Double?, //추가
    val roles: List<UserRole>? = emptyList(), //추가
    val name: String,
    val nickname: String,
    val email: String,
    val schoolId: Long,
    val schoolName: String,
    val profileImageLink: String,
    val status: String
)

//프로필 정보 담는 도메인 모델
data class ProfileInfo(
    val id : Long,
    val linkedIn : String,
    val instagram : String,
    val github : String,
    val blog : String,
    val personal : String,
){
    companion object {
        fun empty() = ProfileInfo(0, "", "", "", "", "")
    }
}


// UserCard 만들기
fun UserInfo.toUserCard(): UserCard {
    // 1. 최신 파트 및 기수 정보 추출
    val currentChallenger = currentGisuMemberInfo?.challenger
    val latestRecord = challengerRecords.maxByOrNull { it.gisu }

    val rawPart = currentChallenger?.part
        ?: latestRecord?.part
        ?: "ADMIN"

    val rawGeneration = currentGisuMemberInfo?.generation?.toString()
        ?: latestRecord?.gisu?.toString()
        ?: "0"

    return UserCard(
        name = name,
        nickname = nickname,
        university = schoolName,
        part = rawPart,                   // String
        generation = rawGeneration,       // String
        avatarURL = profileImageLink,     // profileImageLink -> avatarURL
        email = email,
        github = profile.github,
        blog = profile.blog,
        qrPayload = ""
    )
}

