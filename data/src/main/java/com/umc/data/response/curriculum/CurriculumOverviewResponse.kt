package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.curriculum.CurriculumOverview
import com.umc.domain.model.curriculum.WeeklyCurriculumOverview

data class CurriculumOverviewResponse(
    @SerializedName("curriculumId")
    val curriculumId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("weeks")
    val weeks: List<WeeklyOverviewResponse>?,
) {
    fun toModel(): CurriculumOverview {
        return CurriculumOverview(
            curriculumId = curriculumId ?: 0L,
            title = title.orEmpty(),
            weeks = weeks.orEmpty().map { week ->
                week.toModel()
            },
        )
    }
}

data class WeeklyOverviewResponse(
    @SerializedName("weeklyCurriculumId")
    val weeklyCurriculumId: Long?,

    @SerializedName("weekNo")
    val weekNo: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("isExtra")
    val isExtra: Boolean?,

    @SerializedName("startsAt")
    val startsAt: String?,

    @SerializedName("endsAt")
    val endsAt: String?,
) {
    fun toModel(): WeeklyCurriculumOverview {
        return WeeklyCurriculumOverview(
            weeklyCurriculumId = weeklyCurriculumId ?: 0L,
            weekNo = weekNo?.toInt() ?: 0,
            title = title.orEmpty(),
            isExtra = isExtra ?: false,
            startsAt = startsAt.orEmpty(),
            endsAt = endsAt.orEmpty(),
        )
    }
}