package com.umc.data.response.curriculum

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.model.study.WorkbookProgress
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus
import java.time.Instant


data class CurriculumOverviewResponse(
    @SerializedName("curriculumId") val curriculumId: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("weeks") val weeks: List<WeeklyOverviewResponse>?
) {
    fun toModel(part: UserPart): StudyProgress = StudyProgress(
        curriculumId = curriculumId ?: 0L,
        curriculumTitle = title.orEmpty(),
        part = part,
        completedCount = 0, // overview는 진행상황 없음
        totalCount = weeks.orEmpty().size,
        workbooks = weeks.orEmpty().map { it.toModel() }
    )
}

data class WeeklyOverviewResponse(
    @SerializedName("weeklyCurriculumId") val weeklyCurriculumId: Long?,
    @SerializedName("weekNo") val weekNo: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("isExtra") val isExtra: Boolean?,
    @SerializedName("startsAt") val startsAt: String?,
    @SerializedName("endsAt") val endsAt: String?
) {
    fun toModel(): WorkbookProgress = WorkbookProgress(
        originalWorkbookId = weeklyCurriculumId ?: 0L,
        weekNo = weekNo?.toInt() ?: 0,
        title = title.orEmpty(),
        description = "",
        missionType = WorkbookMissionType.UNKNOWN,
        status = WorkbookStatus.UNKNOWN,
        isReleased = isReleasedNow(),
        isInProgress = isInProgressNow()
    )

    private fun parseMillis(dateStr: String): Long? {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun isReleasedNow(): Boolean {
        val starts = startsAt ?: return false
        val startsMillis = parseMillis(starts) ?: return false
        return System.currentTimeMillis() > startsMillis
    }

    private fun isInProgressNow(): Boolean {
        val starts = startsAt ?: return false
        val ends = endsAt ?: return false
        val startsMillis = parseMillis(starts) ?: return false
        val endsMillis = parseMillis(ends) ?: return false
        val now = System.currentTimeMillis()
        return now > startsMillis && now < endsMillis
    }
}