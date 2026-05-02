package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.model.study.WorkbookProgress

data class CurriculumProgressResponse(
    @SerializedName("curriculumId") val curriculumId: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("weeks") val weeks: List<WeeklyProgressResponse>?
) {
    fun toModel(): StudyProgress = StudyProgress(
        curriculumId = curriculumId ?: 0L,
        curriculumTitle = title.orEmpty(),
        part = UserPart.UNKNOWN,
        completedCount = weeks.orEmpty().count { week ->
            week.originalWorkbooks.orEmpty().any { it.status == "PASS" }
        },
        totalCount = weeks.orEmpty().size,
        workbooks = weeks.orEmpty().flatMap { week ->
            week.originalWorkbooks.orEmpty().map { it.toModel(week.weekNo) }
        }
    )
}
data class WeeklyProgressResponse(
    @SerializedName("weeklyCurriculumId") val weeklyCurriculumId: Long?,
    @SerializedName("weekNo") val weekNo: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("originalWorkbooks") val originalWorkbooks: List<WorkbookProgressResponse>?
)

data class WorkbookProgressResponse(
    @SerializedName("originalWorkbookId") val originalWorkbookId: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("missions") val missions: List<MissionProgressResponse>?,
    @SerializedName("isDeployedToMember") val isDeployedToMember: Boolean?,
    @SerializedName("challengerWorkbookId") val challengerWorkbookId: Long?,
    @SerializedName("status") val status: String?   // 추가 필요
) {
    fun toModel(weekNo: Int?): WorkbookProgress = WorkbookProgress(
        originalWorkbookId = originalWorkbookId ?: 0L,
        weekNo = weekNo ?: 0,
        title = title.orEmpty(),
        description = description.orEmpty(),
        missionType = WorkbookMissionType.LINK, // missions에서 파악
        status = WorkbookStatus.from(status),
        isReleased = isDeployedToMember ?: false,
        isInProgress = status == "IN_PROGRESS"
    )
}

data class MissionProgressResponse(
    @SerializedName("originalWorkbookMissionId") val originalWorkbookMissionId: Long?,
    @SerializedName("missionType") val missionType: String?,
    @SerializedName("hasSubmission") val hasSubmission: Boolean?,
    @SerializedName("submission") val submission: SubmissionResponse?
)

data class SubmissionResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("submittedContent") val submittedContent: String?
)