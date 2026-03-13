package com.umc.data.dataSource.remote.workbook

import com.umc.data.api.WorkbookApi
import com.umc.data.mapper.toFailState
import com.umc.data.request.workbook.BestWorkbookRequest
import com.umc.data.request.workbook.ReviewWorkbookRequest
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.ChallengerWorkbookSubmission
import javax.inject.Inject

class WorkbookRemoteDataSourceImpl @Inject constructor(
    private val api: WorkbookApi
) : WorkbookRemoteDataSource {

    override suspend fun selectBestWorkbook(
        challengerWorkbookId: Long,
        reason: String?
    ): ApiState<Unit> =
        try {
            api.selectBestWorkbook(
                challengerWorkbookId = challengerWorkbookId,
                body = BestWorkbookRequest(bestReason = reason)
            )
            ApiState.Success(Unit)
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }

    override suspend fun reviewWorkbook(
        challengerWorkbookId: Long,
        status: String,
        feedback: String?
    ): ApiState<Unit> =
        try {
            api.reviewWorkbook(
                challengerWorkbookId = challengerWorkbookId,
                body = ReviewWorkbookRequest(status = status, feedback = feedback)
            )
            ApiState.Success(Unit)
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }

    override suspend fun getChallengerWorkbookSubmission(
        challengerWorkbookId: Long
    ): ApiState<ChallengerWorkbookSubmission> {
        return try {
            val res = api.getChallengerWorkbookSubmission(challengerWorkbookId)

            val body = res.result ?: throw IllegalStateException(
                "getChallengerWorkbookSubmission: result is null (code=${res.code}, message=${res.message})"
            )

            ApiState.Success(body.toModel())
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }
}
