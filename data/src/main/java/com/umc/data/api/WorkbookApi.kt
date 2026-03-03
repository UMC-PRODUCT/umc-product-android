package com.umc.data.api

import com.umc.data.remote.response.curriculum.ChallengerWorkbookSubmissionResponse
import com.umc.data.request.workbook.BestWorkbookRequest
import com.umc.data.request.workbook.ReviewWorkbookRequest
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface WorkbookApi {
    @PATCH(Endpoints.Workbook.BEST)
    suspend fun selectBestWorkbook(
        @Path("challengerWorkbookId") challengerWorkbookId: Long,
        @Body body: BestWorkbookRequest
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Workbook.REVIEW)
    suspend fun reviewWorkbook(
        @Path("challengerWorkbookId") challengerWorkbookId: Long,
        @Body body: ReviewWorkbookRequest
    ): ApiResponse<Unit>

    @GET(Endpoints.Workbook.CHALLENGER_SUBMISSION)
    suspend fun getChallengerWorkbookSubmission(
        @Path("challengerWorkbookId") challengerWorkbookId: Long
    ): ApiResponse<ChallengerWorkbookSubmissionResponse>
}