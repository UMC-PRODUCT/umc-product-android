package com.umc.data.response.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OriginalWorkbookResponse(
    @SerialName("originalWorkbookId")
    val originalWorkbookId: Long,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("url")
    val url: String,

    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String,

    @SerialName("status")
    val status: String,

    @SerialName("releasedAt")
    val releasedAt: String?,

    @SerialName("releasedMemberId")
    val releasedMemberId: Long?,

    @SerialName("missions")
    val missions: OriginalWorkbookMissionResponse?
)

@Serializable
data class OriginalWorkbookMissionResponse(
    @SerialName("originalWorkbookId")
    val originalWorkbookId: Long,

    @SerialName("originalWorkbookMissionId")
    val originalWorkbookMissionId: Long,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("missionType")
    val missionType: String,

    @SerialName("isNecessary")
    val isNecessary: Boolean
)