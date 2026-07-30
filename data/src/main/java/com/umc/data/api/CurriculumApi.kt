package com.umc.data.api

import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.AvailableWeeksResponse
import com.umc.data.response.curriculum.ChallengerWorkbookResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.data.response.curriculum.OriginalWorkbookResponse
import com.umc.data.response.curriculum.StudyGroupResponse
import com.umc.data.request.curriculum.CreateBestWorkbookRequest
import com.umc.data.request.curriculum.CreateMissionFeedbackRequest
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CurriculumApi {

    @GET(Endpoints.Curriculum.CURRICULUM_OVERVIEW)
    suspend fun getCurriculumOverview(
        @Query("gisuId") gisuId: Long,
        @Query("part") part: String,
        @Query("weekNo") weekNo: Long? = null,
    ): ApiResponse<CurriculumOverviewResponse>

    @GET(Endpoints.Curriculum.CHALLENGER_ME_PROGRESS)
    suspend fun getMyCurriculumProgress(
        @Query("gisuId") gisuId: Long,
    ): ApiResponse<CurriculumProgressResponse>

    @POST(Endpoints.Curriculum.SUBMIT)
    suspend fun submitChallengerWorkbook(
        @Body body: ChallengerWorkbookSubmitRequest,
    ): ApiResponse<Unit>

    @GET(Endpoints.Curriculum.WORKBOOK_SUBMISSIONS)
    suspend fun getWorkbookSubmissions(
        @Query("weekNo") weekNo: Int,
        @Query("studyGroupId") studyGroupId: Long? = null,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20,
    ): ApiResponse<WorkbookSubmissionsResponse>

    @GET(Endpoints.Curriculum.STUDY_GROUPS)
    suspend fun getStudyGroups(
        @Query("schoolId") schoolId: Long,
        @Query("part") part: String,
    ): ApiResponse<List<StudyGroupResponse>>

    @GET(Endpoints.Curriculum.AVAILABLE_WEEKS)
    suspend fun getAvailableWeeks(): ApiResponse<AvailableWeeksResponse>



    @POST(Endpoints.Curriculum.CREATE_WEEKLY_BEST_WORKBOOK)
    suspend fun createWeeklyBestWorkbook(
        @Body
        body: CreateBestWorkbookRequest,
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Curriculum.UPDATE_WEEKLY_BEST_WORKBOOK)
    suspend fun updateWeeklyBestWorkbook(
        @Path("weeklyBestWorkbookId")
        weeklyBestWorkbookId: Long,

        @Body
        reason: String,
    ): ApiResponse<Unit>

    @DELETE(Endpoints.Curriculum.DELETE_WEEKLY_BEST_WORKBOOK)
    suspend fun deleteWeeklyBestWorkbook(
        @Path("weeklyBestWorkbookId")
        weeklyBestWorkbookId: Long,
    ): ApiResponse<Unit>

    @POST(Endpoints.Curriculum.CREATE_MISSION_FEEDBACK)
    suspend fun createMissionFeedback(
        @Body
        body: CreateMissionFeedbackRequest,
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Curriculum.UPDATE_MISSION_FEEDBACK)
    suspend fun updateMissionFeedback(
        @Path("missionFeedbackId")
        missionFeedbackId: Long,

        @Body
        content: String,
    ): ApiResponse<Unit>

    @GET(Endpoints.Curriculum.CHALLENGER_WORKBOOK_DETAIL)
    suspend fun getChallengerWorkbookDetail(
        @Path("challengerWorkbookId")
        challengerWorkbookId: Long,
    ): ApiResponse<ChallengerWorkbookResponse>
}