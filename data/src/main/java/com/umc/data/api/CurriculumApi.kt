package com.umc.data.api

import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.AvailableWeeksResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.data.response.curriculum.StudyGroupResponse
import retrofit2.http.PATCH

interface CurriculumApi {

    @GET(Endpoints.Curriculum.CURRICULUM_OVERVIEW)
    suspend fun getCurriculumOverview(
        @Query("gisuId") gisuId: Long,
        @Query("part") part: String,
        @Query("weekNo") weekNo: Long? = null
    ): ApiResponse<CurriculumOverviewResponse>

    @POST(Endpoints.Curriculum.SUBMIT)
    suspend fun submitChallengerWorkbook(
        @Body body: ChallengerWorkbookSubmitRequest
    ): ApiResponse<Unit>

    @GET(Endpoints.Curriculum.WORKBOOK_SUBMISSIONS)
    suspend fun getWorkbookSubmissions(
        @Query("weekNo") weekNo: Int,
        @Query("studyGroupId") studyGroupId: Long? = null,
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20
    ): ApiResponse<WorkbookSubmissionsResponse>


    @GET(Endpoints.Curriculum.STUDY_GROUPS)
    suspend fun getStudyGroups(
        @Query("schoolId") schoolId: Long,
        @Query("part") part: String
    ): ApiResponse<List<StudyGroupResponse>>

    @GET(Endpoints.Curriculum.AVAILABLE_WEEKS)
    suspend fun getAvailableWeeks(): ApiResponse<AvailableWeeksResponse>



}
