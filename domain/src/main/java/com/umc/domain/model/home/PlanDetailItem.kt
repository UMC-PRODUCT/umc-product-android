package com.umc.domain.model.home

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.CategoryType

data class PlanDetailItem (
    val scheduleId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val tags: List<CategoryType> = emptyList(),
    val startDay: String = "",   // "2026.02.05"
    val startTime: String = "",  // "06:20"
    val endDay: String = "",     // "2026.02.05"
    val endTime: String = "",    // "06:20"
    val isAllDay: Boolean = false,
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "",
    val dDay: Int = -1,
    //val participantMemberIds: List<Long> = emptyList(), // <- 사용 X
    val participantMembers: List<ParticipantMember> = emptyList(),
    val requiresAttendanceApproval: Boolean = false,

    //작성자 및 참여 정보
    val authorMemberId: Long = -1L,
    val isParticipant: Boolean = false,
    val isAttendanceChecked: Boolean = false,

    //온라인 여부
    val isOnline: Boolean = false,

    //출석 정책 (Attendance Policy) 추가
    val checkInStartDay: String = "", // 출석 시작 가능 시간 (ISO UTC 문자열)
    val checkInStartTime: String = "",
    val onTimeEndDay: String = "",    // 지각 처리 전 출석 마감 시간
    val onTimeEndTime: String = "",
    val lateEndDay: String = "",       // 결석 처리 전 최종 마감 시간
    val lateEndTime: String = ""
)

data class ParticipantMember(
    val memberId: Long,
    val name: String?,
    val nickname: String?,
    val schoolId: Long?,
    val schoolName: String?,
    val profileImageUrl: String?
)