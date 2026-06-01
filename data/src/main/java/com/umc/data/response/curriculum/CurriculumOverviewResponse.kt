package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.model.study.WeeklyCurriculum

data class CurriculumOverviewResponse(
    @SerializedName("curriculumId") val curriculumId: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("weeks") val weeks: List<WeeklyCurriculumResponse>?
) {
    fun toModel(): StudyProgress = StudyProgress(
        curriculumId = curriculumId ?: 0L,
        curriculumTitle = title.orEmpty(),
        weeks = weeks.orEmpty().map { it.toModel() }
    )
}

data class WeeklyCurriculumResponse(
    @SerializedName("weeklyCurriculumId") val weeklyCurriculumId: Long?,
    @SerializedName("weekNo") val weekNo: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("isExtra") val isExtra: Boolean?,
    @SerializedName("startsAt") val startsAt: String?,
    @SerializedName("endsAt") val endsAt: String?
) {
    fun toModel(): WeeklyCurriculum = WeeklyCurriculum(
        weeklyCurriculumId = weeklyCurriculumId ?: 0L,
        weekNo = weekNo ?: 0,
        title = title.orEmpty(),
        isExtra = isExtra ?: false,
        startsAt = startsAt.orEmpty(),
        endsAt = endsAt.orEmpty()
    )
}