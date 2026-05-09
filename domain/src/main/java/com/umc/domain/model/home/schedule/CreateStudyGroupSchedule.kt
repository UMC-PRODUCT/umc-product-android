package com.umc.domain.model.home.schedule

data class CreateStudyGroupSchedule(
    val name: String,
    val startsAt: String,
    val endsAt: String,
    val isAllDay: Boolean,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val tags: List<String>,
    val studyGroupId: Long,
    val gisuId: Long,
    val requiresApproval: Boolean,
    val participantMemberIds: List<Long> = emptyList(),
    val attendancePolicy: AttendancePolicy? = null,
) {
    data class AttendancePolicy(
        val checkInStartAt: String,
        val onTimeEndAt: String,
        val lateEndAt: String,
    )
}