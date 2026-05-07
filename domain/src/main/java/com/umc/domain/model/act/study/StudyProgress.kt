package com.umc.domain.model.study

data class StudyProgress(
    val curriculumId: Long,
    val curriculumTitle: String,
    val weeks: List<WeeklyCurriculum>
)

data class WeeklyCurriculum(
    val weeklyCurriculumId: Long,
    val weekNo: Int,
    val title: String,
    val isExtra: Boolean,
    val startsAt: String,
    val endsAt: String
)