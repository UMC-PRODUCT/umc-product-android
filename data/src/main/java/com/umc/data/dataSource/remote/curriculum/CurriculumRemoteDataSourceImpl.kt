package com.umc.data.dataSource.remote.curriculum

import com.umc.data.api.CurriculumApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.dataSource.base.requireResult
import com.umc.data.request.curriculum.ChallengerWorkbookSubmitRequest
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject
import com.umc.data.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.data.request.curriculum.CreateBestWorkbookRequest
import com.umc.data.request.curriculum.CreateMissionFeedbackRequest
import com.umc.data.response.curriculum.ChallengerWorkbookResponse
import com.umc.data.response.curriculum.WeeklyBestWorkbooksResponse
import com.umc.data.response.curriculum.WorkbookSubmissionsV2Response
import com.umc.domain.model.base.map
import com.umc.domain.model.curriculum.StudyGroup

/**
 * 모든 엔드포인트를 apiCall 로 감싸 서버 오류 본문(code·message)과 네트워크 단절을 다른 API 와 같게 분류하고,
 * success=false 응답을 실패로, 취소는 다시 던진다.
 * CurriculumRepositoryImpl 이 값을 바로 화면 모델로 바꾸므로 result 가 비어 온 성공 응답은 requireResult 로 실패로 돌린다.
 */
class CurriculumRemoteDataSourceImpl @Inject constructor(
    private val curriculumApi: CurriculumApi
) : CurriculumRemoteDataSource {

    override suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverviewResponse> {
        return apiCall {
            curriculumApi.getCurriculumOverview(gisuId, part)
        }.requireResult()
    }

    override suspend fun getMyCurriculumProgress(gisuId: Long): ApiState<CurriculumProgressResponse> {
        return apiCall {
            curriculumApi.getMyCurriculumProgress(gisuId)
        }.requireResult()
    }

    override suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.submitChallengerWorkbook(
                body = ChallengerWorkbookSubmitRequest(
                    submission = submission,
                    originalWorkbookId = originalWorkbookId
                )
            )
        }.map { }
    }

    override suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int
    ): ApiState<WorkbookSubmissionsResponse> {
        return apiCall {
            curriculumApi.getWorkbookSubmissions(
                weekNo = weekNo,
                studyGroupId = studyGroupId,
                cursor = cursor,
                size = size
            )
        }.requireResult()
    }

    // 운영진 - 스터디원 제출 현황 조회
    override suspend fun getWorkbookSubmissionsV2(
        studyGroupId: Long?,
        weekNos: List<Long>?,
        cursor: Long?,
        size: Int,
    ): ApiState<WorkbookSubmissionsV2Response> {
        return apiCall {
            curriculumApi.getWorkbookSubmissionsV2(
                studyGroupId = studyGroupId,
                weekNos = weekNos,
                cursor = cursor,
                size = size,
            )
        }.requireResult()
    }

    // 운영진 - 제출 현황 조회 가능 주차 목록
    override suspend fun getWorkbookSubmissionWeeks(
        studyGroupId: Long?,
    ): ApiState<List<Long>> {
        return apiCall {
            curriculumApi.getWorkbookSubmissionWeeks(
                studyGroupId = studyGroupId,
            )
        }.requireResult()
    }

    // 운영진 - 베스트 워크북 조회
    override suspend fun getWeeklyBestWorkbooks(
        gisuId: Long?,
        schoolIds: List<Long>?,
        parts: List<String>?,
        weekNos: List<Long>?,
        studyGroupIds: List<Long>?,
        page: Int,
        size: Int,
    ): ApiState<WeeklyBestWorkbooksResponse> {
        return apiCall {
            curriculumApi.getWeeklyBestWorkbooks(
                gisuId = gisuId,
                schoolIds = schoolIds,
                parts = parts,
                weekNos = weekNos,
                studyGroupIds = studyGroupIds,
                page = page,
                size = size,
            )
        }.requireResult()
    }


    override suspend fun getStudyGroups(
        schoolId: Long,
        part: String
    ): ApiState<List<StudyGroup>> {
        // result 가 비어 오면 빈 목록으로 본다
        return apiCall { curriculumApi.getStudyGroups(schoolId, part) }
            .mapNullable { groups -> groups?.map { it.toDomain() } ?: emptyList() }
    }

    override suspend fun getAvailableWeeks(): ApiState<List<Int>> {
        // result 가 비어 오면 빈 목록으로 본다
        return apiCall { curriculumApi.getAvailableWeeks() }
            .mapNullable { response -> response?.weeks ?: emptyList() }
    }

    override suspend fun getChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbookResponse> {
        return apiCall {
            curriculumApi.getChallengerWorkbookDetail(
                challengerWorkbookId = challengerWorkbookId,
            )
        }.requireResult()
    }

    override suspend fun createWeeklyBestWorkbook(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.createWeeklyBestWorkbook(
                body = CreateBestWorkbookRequest(
                    bestMemberId = bestMemberId,
                    weeklyCurriculumId = weeklyCurriculumId,
                    studyGroupId = studyGroupId,
                    reason = reason,
                ),
            )
        }.map { }
    }

    override suspend fun updateWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.updateWeeklyBestWorkbook(
                weeklyBestWorkbookId = weeklyBestWorkbookId,
                reason = reason,
            )
        }.map { }
    }

    override suspend fun deleteWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.deleteWeeklyBestWorkbook(
                weeklyBestWorkbookId = weeklyBestWorkbookId,
            )
        }.map { }
    }

    override suspend fun createMissionFeedback(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.createMissionFeedback(
                body = CreateMissionFeedbackRequest(
                    missionSubmissionId = missionSubmissionId,
                    content = content,
                    result = result,
                ),
            )
        }.map { }
    }

    override suspend fun updateMissionFeedback(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit> {
        return apiCall {
            curriculumApi.updateMissionFeedback(
                missionFeedbackId = missionFeedbackId,
                content = content,
            )
        }.map { }
    }

    // result 가 null 일 수 있는 응답을 기본값으로 바꿀 때 쓴다. map 은 null 을 받는 쪽을 가정하지 않는다.
    private fun <T, R> ApiState<T>.mapNullable(transform: (T?) -> R): ApiState<R> {
        return when (this) {
            is ApiState.Success -> ApiState.Success(transform(data))
            is ApiState.Fail -> this
        }
    }
}

