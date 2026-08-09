package com.umc.domain.model.request.organization

data class CreateStudyGroupScheduleRequest(
    val studyGroupId: Long,
    val scheduleId: Long,
    val weeklyCurriculumId: Long,
)