package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.model.study.WorkbookProgress

data class CurriculumProgressResponse(
    @SerializedName("curriculumId") val curriculumId: String?,
    @SerializedName("curriculumTitle") val curriculumTitle: String?,
    @SerializedName("part") val part: String?,
    @SerializedName("completedCount") val completedCount: String?,
    @SerializedName("totalCount") val totalCount: String?,
    @SerializedName("workbooks") val workbooks: List<WorkbookProgressResponse>?
) {
    fun toModel(): StudyProgress = StudyProgress(
        curriculumId = curriculumId?.toLongOrNull() ?: 0L,
        curriculumTitle = curriculumTitle.orEmpty(),
        part = UserPart.from(part),
        completedCount = completedCount?.toIntOrNull() ?: 0,
        totalCount = totalCount?.toIntOrNull() ?: 0,
        workbooks = workbooks.orEmpty().map { it.toModel() }
    )

}

data class WorkbookProgressResponse(
    @SerializedName("challengerWorkbookId") val challengerWorkbookId: String?,
    @SerializedName("weekNo") val weekNo: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("missionType") val missionType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("isReleased") val isReleased: Boolean?,
    @SerializedName("isInProgress") val isInProgress: Boolean?
) {
    fun toModel(): WorkbookProgress = WorkbookProgress(
        challengerWorkbookId = challengerWorkbookId?.toLongOrNull() ?: 0L,
        weekNo = weekNo?.toIntOrNull() ?: 0,
        title = title.orEmpty(),
        description = description.orEmpty(),
        missionType = WorkbookMissionType.from(missionType),
        status = WorkbookStatus.from(status),
        isReleased = isReleased ?: false,
        isInProgress = isInProgress ?: false
    )

}
