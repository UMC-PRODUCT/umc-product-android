package com.umc.domain.model.home.schedule

data class UpdateSchedule(
    val name: String,
    val description: String,
    val tags: List<String>,
    val startsAt: String,
    val endsAt: String,
    // null = 기존 장소 유지, non-null = 장소 변경 (isOnline=false 와 함께 사용)
    val location: Location?,
    // null = 기존 상태 유지, true = 비대면 전환, false = 대면 전환(location 필수)
    val isOnline: Boolean? = null,
    // null = 기존 상태 유지, true = 출석 O(attendancePolicy 필수), false = 출석 X
    val isAttendanceRequired: Boolean?,
    val attendancePolicy: AttendancePolicy?,
    val participantMemberIds: List<Long>
) {
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    )

    data class AttendancePolicy(
        val checkInStartAt: String,
        val onTimeEndAt: String,
        val lateEndAt: String
    )
}
