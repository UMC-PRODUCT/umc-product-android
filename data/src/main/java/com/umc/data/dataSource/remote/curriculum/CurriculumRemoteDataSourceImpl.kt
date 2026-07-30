package com.umc.data.dataSource.remote.curriculum

import com.umc.data.api.CurriculumApi
import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject
import com.umc.data.mapper.toFailState
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.domain.model.base.FailState
import com.umc.data.request.curriculum.CreateBestWorkbookRequest
import com.umc.data.request.curriculum.CreateMissionFeedbackRequest
import com.umc.data.response.curriculum.ChallengerWorkbookResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.curriculum.StudyGroup


class CurriculumRemoteDataSourceImpl @Inject constructor(
    private val curriculumApi: CurriculumApi
) : CurriculumRemoteDataSource {

    override suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverviewResponse> {
        return fetch {
            curriculumApi.getCurriculumOverview(gisuId, part)
        }
    }

    override suspend fun getMyCurriculumProgress(gisuId: Long): ApiState<CurriculumProgressResponse> {
        return fetch {
            curriculumApi.getMyCurriculumProgress(gisuId)
        }
    }

    override suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String
    ): ApiState<Unit> {
        return try {
            val res = curriculumApi.submitChallengerWorkbook(
                body = ChallengerWorkbookSubmitRequest(
                    submission = submission,
                    originalWorkbookId = originalWorkbookId
                )
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

    override suspend fun getChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbookResponse> {
        return fetch {
            curriculumApi.getChallengerWorkbookDetail(
                challengerWorkbookId = challengerWorkbookId,
            )
        }
    }

    override suspend fun createWeeklyBestWorkbook(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit> {
        return fetchUnit {
            curriculumApi.createWeeklyBestWorkbook(
                body = CreateBestWorkbookRequest(
                    bestMemberId = bestMemberId,
                    weeklyCurriculumId = weeklyCurriculumId,
                    studyGroupId = studyGroupId,
                    reason = reason,
                ),
            )
        }
    }

    override suspend fun updateWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit> {
        return fetchUnit {
            curriculumApi.updateWeeklyBestWorkbook(
                weeklyBestWorkbookId = weeklyBestWorkbookId,
                reason = reason,
            )
        }
    }

    override suspend fun deleteWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit> {
        return fetchUnit {
            curriculumApi.deleteWeeklyBestWorkbook(
                weeklyBestWorkbookId = weeklyBestWorkbookId,
            )
        }
    }

    override suspend fun createMissionFeedback(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit> {
        return fetchUnit {
            curriculumApi.createMissionFeedback(
                body = CreateMissionFeedbackRequest(
                    missionSubmissionId = missionSubmissionId,
                    content = content,
                    result = result,
                ),
            )
        }
    }

    override suspend fun updateMissionFeedback(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit> {
        return fetchUnit {
            curriculumApi.updateMissionFeedback(
                missionFeedbackId = missionFeedbackId,
                content = content,
            )
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

    private suspend fun fetchUnit(
        call: suspend () -> ApiResponse<Unit>,
    ): ApiState<Unit> {
        return try {
            val response = call()

            if (response.success) {
                ApiState.Success(Unit)
            } else {
                ApiState.Fail(
                    FailState(
                        false,
                        response.code,
                        response.message,
                    ),
                )
            }
        } catch (e: Exception) {
            ApiState.Fail(
                FailState(
                    false,
                    "UNKNOWN",
                    e.message ?: "알 수 없는 오류",
                ),
            )
        }
    }
}

