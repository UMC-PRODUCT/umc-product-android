package com.umc.data.request.schedule

import com.google.gson.annotations.SerializedName

data class CreateStudyGroupScheduleRequest(
    @SerializedName("studyGroupId") val studyGroupId: Long,
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("weeklyCurriculumId") val weeklyCurriculumId: Long,
)
