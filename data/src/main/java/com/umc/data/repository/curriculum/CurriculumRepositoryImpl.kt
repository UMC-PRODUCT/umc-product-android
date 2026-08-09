package com.umc.data.repository.curriculum

import com.umc.data.dataSource.remote.curriculum.CurriculumRemoteDataSource
import com.umc.data.mapper.curriculum.toDomain
import com.umc.domain.model.act.study.StudyProgress
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.CursorPage
import com.umc.domain.model.base.map
import com.umc.domain.model.curriculum.CurriculumOverview
import com.umc.domain.model.curriculum.StudyGroup
import com.umc.domain.model.curriculum.WorkbookSubmissionItem
import com.umc.domain.repository.curriculum.CurriculumRepository
import com.umc.domain.model.curriculum.ChallengerWorkbook
import com.umc.domain.model.curriculum.StudyMemberSubmissionPage
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val remote: CurriculumRemoteDataSource,
) : CurriculumRepository {

    override suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverview> {
        return when (
            val result = remote.getCurriculumOverview(
                gisuId = gisuId,
                part = part,
            )
        ) {
            is ApiState.Success -> {
                ApiState.Success(
                    result.data.toModel()
                )
            }

            is ApiState.Fail -> result
        }
    }

    override suspend fun getMyCurriculumProgress(
        gisuId: Long,
    ): ApiState<StudyProgress> {
        return when (
            val result = remote.getMyCurriculumProgress(
                gisuId = gisuId,
            )
        ) {
            is ApiState.Success -> {
                ApiState.Success(
                    result.data.toModel()
                )
            }

            is ApiState.Fail -> result
        }
    }

    override suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String,
    ) = remote.submitChallengerWorkbook(
        originalWorkbookId = originalWorkbookId,
        submission = submission,
    )

    override suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int,
    ): ApiState<CursorPage<WorkbookSubmissionItem>> {
        return remote.getWorkbookSubmissions(
            weekNo = weekNo,
            studyGroupId = studyGroupId,
            cursor = cursor,
            size = size,
        ).map { response ->
            response.toDomain()
        }
    }

    // 운영진 - 스터디원 제출 현황 조회
    override suspend fun getWorkbookSubmissionsV2(
        studyGroupId: Long?,
        weekNos: List<Long>?,
        cursor: Long?,
        size: Int,
    ): ApiState<StudyMemberSubmissionPage> {
        return remote.getWorkbookSubmissionsV2(
            studyGroupId = studyGroupId,
            weekNos = weekNos,
            cursor = cursor,
            size = size,
        ).map { response ->
            response.toDomain()
        }
    }

    // 운영진 - 제출 현황 조회 가능 주차 목록
    override suspend fun getWorkbookSubmissionWeeks(
        studyGroupId: Long?,
    ): ApiState<List<Long>> {
        return remote.getWorkbookSubmissionWeeks(
            studyGroupId = studyGroupId,
        )
    }

    override suspend fun getStudyGroups(
        schoolId: Long,
        part: String,
    ): ApiState<List<StudyGroup>> {
        return remote.getStudyGroups(
            schoolId = schoolId,
            part = part,
        )
    }

    override suspend fun getAvailableWeeks(): ApiState<List<Int>> {
        return remote.getAvailableWeeks()
    }

    override suspend fun getChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbook> {
        return when (
            val result = remote.getChallengerWorkbookDetail(
                challengerWorkbookId = challengerWorkbookId,
            )
        ) {
            is ApiState.Success -> {
                ApiState.Success(result.data.toDomain())
            }

            is ApiState.Fail -> result
        }
    }

    override suspend fun createWeeklyBestWorkbook(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit> {
        return remote.createWeeklyBestWorkbook(
            bestMemberId = bestMemberId,
            weeklyCurriculumId = weeklyCurriculumId,
            studyGroupId = studyGroupId,
            reason = reason,
        )
    }

    override suspend fun updateWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit> {
        return remote.updateWeeklyBestWorkbook(
            weeklyBestWorkbookId = weeklyBestWorkbookId,
            reason = reason,
        )
    }

    override suspend fun deleteWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit> {
        return remote.deleteWeeklyBestWorkbook(
            weeklyBestWorkbookId = weeklyBestWorkbookId,
        )
    }

    override suspend fun createMissionFeedback(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit> {
        return remote.createMissionFeedback(
            missionSubmissionId = missionSubmissionId,
            content = content,
            result = result,
        )
    }

    override suspend fun updateMissionFeedback(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit> {
        return remote.updateMissionFeedback(
            missionFeedbackId = missionFeedbackId,
            content = content,
        )
    }
}