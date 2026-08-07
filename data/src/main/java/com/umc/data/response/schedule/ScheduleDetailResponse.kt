package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.home.ParticipantMember
import com.umc.domain.model.home.PlanDetailItem

data class ScheduleDetailResponse(
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("tags") val tags: List<CategoryType>?,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("authorMemberId") val authorMemberId: Long? = null,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("isAllDay") val isAllDay: Boolean,
    @SerializedName("isOnline") val isOnline: Boolean = false,
    @SerializedName("location") val location: ScheduleDetailLocationResponse? = null,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("attendanceStatus") val attendanceStatus: String? = null,
    @SerializedName("isAttendanceChecked") val isAttendanceChecked: Boolean? = null,
    @SerializedName("isParticipant") val isParticipant: Boolean? = null,
    @SerializedName("dDay") val dDay: Int?,
    @SerializedName("requiresAttendanceApproval") val requiresAttendanceApproval: Boolean?,
    //@SerializedName("participantMemberIds") val participantMemberIds: List<Long>?,
    @SerializedName("attendancePolicy") val attendancePolicy: AttendancePolicyResponse? = null,
    @SerializedName("participants") val participants: List<ParticipantResponse>? = null,
) {
    companion object {
        fun ScheduleDetailResponse.toModel(): UserCheckAvailable {
            val resolvedLocationName = location?.locationName ?: locationName.orEmpty()
            val resolvedLatitude = location?.latitude ?: latitude ?: 0.0
            val resolvedLongitude = location?.longitude ?: longitude ?: 0.0

            return UserCheckAvailable(
                /**TODO: 후에 Long 마이그레이션 시 해당 부분 교체 요망**/
                id = scheduleId,
                title = name,
                tags = tags,
                sheetId = 0,
                startTime = startsAt,
                endTime = endsAt,
                status = CheckAvailableStatus.BEFORE,
                latitude = resolvedLatitude,
                longitude = resolvedLongitude,
                address = resolvedLocationName,
                isLocationCertified = null,
                isOnline = isOnline,
            )
        }

        fun ScheduleDetailResponse.toPlanDetailDomain(): PlanDetailItem {
            // "T"를 기준으로 날짜와 시간을 분리
            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()

            val resolvedLocationName = location?.locationName ?: locationName.orEmpty()
            val resolvedLatitude = location?.latitude ?: latitude ?: 0.0
            val resolvedLongitude = location?.longitude ?: longitude ?: 0.0

            return PlanDetailItem(
                scheduleId = scheduleId,
                name = name,
                description = description ?: "",
                tags = tags ?: emptyList(),
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                isAllDay = isAllDay,
                locationName = resolvedLocationName,
                latitude = resolvedLatitude,
                longitude = resolvedLongitude,
                status = status ?: "",
                dDay = dDay ?: -1,
                requiresAttendanceApproval = requiresAttendanceApproval ?: false,
                //participantMemberIds = participantMemberIds ?: emptyList(),
                //출석 인원 추가
                participantMembers = participants?.map { participant ->
                    ParticipantMember(
                        memberId = participant.memberId,
                        name = participant.name,
                        nickname = participant.nickname,
                        schoolId = participant.schoolId,
                        schoolName = participant.schoolName,
                        profileImageUrl = participant.profileImageUrl
                    )
                } ?: emptyList(),
                // 출석 관련 추가
                authorMemberId = authorMemberId ?: -1L,
                isParticipant = isParticipant ?: false,
                isAttendanceChecked = isAttendanceChecked ?: false,

                // 출석 정책 매핑
                checkInStartAt = attendancePolicy?.checkInStartAt.orEmpty(),
                onTimeEndAt = attendancePolicy?.onTimeEndAt.orEmpty(),
                lateEndAt = attendancePolicy?.lateEndAt.orEmpty(),
            )
        }
    }
}

data class ScheduleDetailLocationResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("locationName") val locationName: String,
)

//출석 정책 Response DTO
data class AttendancePolicyResponse(
    @SerializedName("checkInStartAt") val checkInStartAt: String?,
    @SerializedName("onTimeEndAt") val onTimeEndAt: String?,
    @SerializedName("lateEndAt") val lateEndAt: String?
)

data class ParticipantResponse(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("schoolId") val schoolId: Long?,
    @SerializedName("schoolName") val schoolName: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)
