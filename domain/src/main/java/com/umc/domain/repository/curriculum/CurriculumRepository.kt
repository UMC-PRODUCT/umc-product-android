package com.umc.domain.repository.curriculum

import com.umc.domain.model.act.study.StudyProgress
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.CursorPage
import com.umc.domain.model.curriculum.ChallengerWorkbook
import com.umc.domain.model.curriculum.CurriculumOverview
import com.umc.domain.model.curriculum.StudyGroup
import com.umc.domain.model.curriculum.StudyMemberSubmissionPage
import com.umc.domain.model.curriculum.WorkbookSubmissionItem

interface CurriculumRepository {

    suspend fun getMyCurriculumProgress(
        gisuId: Long,
    ): ApiState<StudyProgress>

    suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String,
    ): ApiState<Unit>

    suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverview>

    suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long? = null,
        cursor: Long? = null,
        size: Int = 20,
    ): ApiState<CursorPage<WorkbookSubmissionItem>>

    suspend fun getStudyGroups(
        schoolId: Long,
        part: String,
    ): ApiState<List<StudyGroup>>

    suspend fun getAvailableWeeks(): ApiState<List<Int>>

    suspend fun getChallengerWorkbookDetail(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbook>

    suspend fun createWeeklyBestWorkbook(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit>

    suspend fun updateWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit>

    suspend fun deleteWeeklyBestWorkbook(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit>

    suspend fun createMissionFeedback(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit>

    suspend fun updateMissionFeedback(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit>


    // 운영진 - 스터디원 제출 현황 조회
    suspend fun getWorkbookSubmissionsV2(
        studyGroupId: Long? = null,
        weekNos: List<Long>? = null,
        cursor: Long? = null,
        size: Int = 20,
    ): ApiState<StudyMemberSubmissionPage>

    // 운영진 - 제출 현황 조회 가능 주차 목록
    suspend fun getWorkbookSubmissionWeeks(
        studyGroupId: Long? = null,
    ): ApiState<List<Long>>
}