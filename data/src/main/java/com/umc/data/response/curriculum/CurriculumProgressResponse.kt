package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import com.umc.domain.model.act.study.StudyProgress
import com.umc.domain.model.act.study.WorkbookProgress

data class CurriculumProgressResponse(
    @SerializedName("curriculumId")
    val curriculumId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("weeks")
    val weeks: List<WeeklyProgressResponse>?,
) {
    fun toModel(): StudyProgress {
        val weekList = weeks.orEmpty()

        return StudyProgress(
            curriculumId = curriculumId ?: 0L,
            curriculumTitle = title.orEmpty(),

            part = UserPart.UNKNOWN,

            completedCount = weekList.count {
                it.status == "PASS"
            },

            totalCount = weekList.size,

            workbooks = weekList.flatMap { week ->
                week.originalWorkbooks.orEmpty().map { workbook ->
                    workbook.toModel(
                        weekNo = week.weekNo?.toInt(),
                        weeklyStatus = week.status,
                    )
                }
            },
        )
    }
}

data class WeeklyProgressResponse(
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

    @SerializedName("status")
    val status: String?,

    @SerializedName("originalWorkbooks")
    val originalWorkbooks: List<WorkbookProgressResponse>?,
)

data class WorkbookProgressResponse(
    @SerializedName("originalWorkbookId")
    val originalWorkbookId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("url")
    val url: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("missions")
    val missions: List<MissionProgressResponse>?,

    @SerializedName("isDeployedToMember")
    val isDeployedToMember: Boolean?,

    @SerializedName("challengerWorkbookId")
    val challengerWorkbookId: Long?,
) {
    fun toModel(
        weekNo: Int?,
        weeklyStatus: String?,
    ): WorkbookProgress {
        val firstMission = missions.orEmpty().firstOrNull()

        return WorkbookProgress(
            originalWorkbookId = originalWorkbookId ?: 0L,
            weekNo = weekNo ?: 0,
            title = title.orEmpty(),
            description = description.orEmpty(),
            missionType = WorkbookMissionType.from(
                firstMission?.missionType
            ),
            status = WorkbookStatus.from(weeklyStatus),
            isReleased = isDeployedToMember ?: false,
            isInProgress = weeklyStatus == "IN_PROGRESS",
        )
    }
}

data class MissionProgressResponse(
    @SerializedName("originalWorkbookMissionId")
    val originalWorkbookMissionId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("missionType")
    val missionType: String?,

    @SerializedName("isNecessary")
    val isNecessary: Boolean?,

    @SerializedName("hasSubmission")
    val hasSubmission: Boolean?,

    @SerializedName("submission")
    val submission: SubmissionResponse?,
)

data class SubmissionResponse(
    @SerializedName("missionSubmissionId")
    val missionSubmissionId: Long?,

    @SerializedName("originalWorkbookMissionId")
    val originalWorkbookMissionId: Long?,

    @SerializedName("submittedAsType")
    val submittedAsType: String?,

    @SerializedName("submittedContent")
    val submittedContent: String?,

    @SerializedName("submittedAt")
    val submittedAt: String?,

    @SerializedName("lastEditedAt")
    val lastEditedAt: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("hasFeedback")
    val hasFeedback: Boolean?,

    @SerializedName("feedbacks")
    val feedbacks: List<MissionFeedbackResponse>?,
)

