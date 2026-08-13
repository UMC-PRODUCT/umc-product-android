package com.umc.data.request.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBestWorkbookRequest(
    @SerialName("bestMemberId")
    val bestMemberId: Long,

    @SerialName("weeklyCurriculumId")
    val weeklyCurriculumId: Long,

    @SerialName("studyGroupId")
    val studyGroupId: Long,

    @SerialName("reason")
    val reason: String,
)