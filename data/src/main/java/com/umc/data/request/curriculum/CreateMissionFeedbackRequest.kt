package com.umc.data.request.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMissionFeedbackRequest(
    @SerialName("missionSubmissionId")
    val missionSubmissionId: Long,

    @SerialName("content")
    val content: String,

    @SerialName("result")
    val result: String,
)