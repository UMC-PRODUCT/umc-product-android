package com.umc.domain.model

import com.umc.domain.model.act.challenger.ChallengerPoint


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
    val challengerRecords: List<ChallengerRecord> = emptyList()
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
    val name: String,
    val nickname: String,
    val email: String,
    val schoolId: Long,
    val schoolName: String,
    val profileImageLink: String,
    val status: String
)

/** ChallengerManagerDialogModel.kt꺼 사용
 * 
 * data class ChallengerPoint(
 *     val id: Long,
 *     val date: String = "",
 *     val title: String,
 *     val pointType: PointType,
 *     val value: Double
 * )
 *
 * **/

