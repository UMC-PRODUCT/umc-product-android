package com.umc.data.dataSource.remote.curriculum

import com.umc.data.mapper.toFailState
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.domain.model.curriculum.StudyGroup
import com.umc.data.response.curriculum.ChallengerWorkbookResponse
import com.umc.data.response.curriculum.WeeklyBestWorkbooksResponse
import com.umc.data.response.curriculum.WorkbookSubmissionsV2Response

interface CurriculumRemoteDataSource {


    suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverviewResponse>

    suspend fun getMyCurriculumProgress(
        gisuId: Long
    ): ApiState<CurriculumProgressResponse>

    suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String
    ): ApiState<Unit>

    suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int,
    ): ApiState<WorkbookSubmissionsResponse>

    // 운영진 - 스터디원 제출 현황 조회
    suspend fun getWorkbookSubmissionsV2(
        studyGroupId: Long?,
        weekNos: List<Long>?,
        cursor: Long?,
        size: Int,
    ): ApiState<WorkbookSubmissionsV2Response>

    // 운영진 - 제출 현황 조회 가능 주차 목록
    suspend fun getWorkbookSubmissionWeeks(
        studyGroupId: Long?,
    ): ApiState<List<Long>>

    // 운영진 - 베스트 워크북 조회
    suspend fun getWeeklyBestWorkbooks(
        gisuId: Long? = null,
        schoolIds: List<Long>? = null,
        parts: List<String>? = null,
        weekNos: List<Long>? = null,
        studyGroupIds: List<Long>? = null,
        page: Int = 0,
        size: Int = 20,
    ): ApiState<WeeklyBestWorkbooksResponse>

    suspend fun getStudyGroups(
        schoolId: Long,
        part: String
    ): ApiState<List<StudyGroup>>

    suspend fun getAvailableWeeks(): ApiState<List<Int>>

    suspend fun getChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbookResponse>

    suspend fun createWeeklyBestWorkbook(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit>

    suspend fun updateWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit>

    suspend fun deleteWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit>

    suspend fun createMissionFeedback(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit>

    suspend fun updateMissionFeedback(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit>
}
