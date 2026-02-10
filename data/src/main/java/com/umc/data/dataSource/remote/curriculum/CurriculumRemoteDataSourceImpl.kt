package com.umc.data.dataSource.remote.curriculum

import com.umc.data.api.CurriculumApi
import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject
import com.umc.data.mapper.toFailState
import com.umc.domain.model.base.FailState


class CurriculumRemoteDataSourceImpl @Inject constructor(
    private val curriculumApi: CurriculumApi
) : CurriculumRemoteDataSource {

    override suspend fun getMyCurriculumProgress(page: Int?, limit: Int?): ApiState<CurriculumProgressResponse> {
        return try {
            val res = curriculumApi.getMyCurriculumProgress(page, limit)
            val body = res.result
            if (body == null) {
                ApiState.Fail(failState = IllegalStateException("Empty body").toFailState())
            } else {
                ApiState.Success(body)
            }
        } catch (e: Exception) {
            ApiState.Fail(failState = e.toFailState())
        }
    }

    override suspend fun submitChallengerWorkbook(
        challengerWorkbookId: Long,
        submission: String
    ): ApiState<Unit> {
        return try {
            val res = curriculumApi.submitChallengerWorkbook(
                challengerWorkbookId = challengerWorkbookId,
                body = ChallengerWorkbookSubmitRequest(submission)
            )
            ApiState.Success(res.result ?: Unit)
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }
}

