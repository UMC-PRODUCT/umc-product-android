package com.umc.data.api

import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CurriculumApi {

    @GET(Endpoints.Curriculum.CHALLENGER_ME_PROGRESS)
    suspend fun getMyCurriculumProgress(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): ApiResponse<CurriculumProgressResponse>

    @POST(Endpoints.Curriculum.SUBMIT)
    suspend fun submitChallengerWorkbook(
        @Path("challengerWorkbookId") challengerWorkbookId: Long,
        @Body body: ChallengerWorkbookSubmitRequest
    ): ApiResponse<Unit>
}
