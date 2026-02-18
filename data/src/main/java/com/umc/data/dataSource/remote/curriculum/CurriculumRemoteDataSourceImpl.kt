package com.umc.data.dataSource.remote.curriculum

import com.umc.data.api.CurriculumApi
import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject
import com.umc.data.mapper.toFailState
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.domain.model.base.FailState

import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.curriculum.StudyGroup


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

    override suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int
    ): ApiState<WorkbookSubmissionsResponse> {
        return fetch {
            curriculumApi.getWorkbookSubmissions(
                weekNo = weekNo,
                studyGroupId = studyGroupId,
                cursor = cursor,
                size = size
            )
        }
    }


    override suspend fun getStudyGroups(
        schoolId: Long,
        part: String
    ): ApiState<List<StudyGroup>> {
        return try {
            val response = curriculumApi.getStudyGroups(schoolId, part)
            ApiState.Success(response.result?.map { it.toDomain() } ?: emptyList())
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }

    override suspend fun getAvailableWeeks(): ApiState<List<Int>> {
        return try {
            val response = curriculumApi.getAvailableWeeks()
            ApiState.Success(response.result?.weeks ?: emptyList())
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }

    private suspend fun <T> fetch(call: suspend () -> ApiResponse<T>): ApiState<T> {
        return try {
            val response = call()
            if (response.success) {
                ApiState.Success(response.result ?: error("Data is null"))
            } else {
                ApiState.Fail(FailState(false, response.code, response.message))
            }
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}

