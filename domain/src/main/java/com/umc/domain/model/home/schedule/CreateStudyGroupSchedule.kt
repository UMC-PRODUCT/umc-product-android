package com.umc.domain.model.home.schedule

data class CreateStudyGroupSchedule(
    val studyGroupId: Long,
    val scheduleId: Long,
    val weeklyCurriculumId: Long,
)
