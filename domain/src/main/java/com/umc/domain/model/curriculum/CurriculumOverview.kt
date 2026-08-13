package com.umc.domain.model.curriculum

data class CurriculumOverview(
    val curriculumId: Long,
    val title: String,
    val weeks: List<WeeklyCurriculumOverview>,
)

data class WeeklyCurriculumOverview(
    val weeklyCurriculumId: Long,
    val weekNo: Int,
    val title: String,
    val isExtra: Boolean,
    val startsAt: String,
    val endsAt: String,
)