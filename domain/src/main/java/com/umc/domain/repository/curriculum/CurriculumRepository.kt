package com.umc.domain.repository.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.study.StudyProgress

import com.umc.domain.model.base.CursorPage
import com.umc.domain.model.curriculum.StudyGroup
import com.umc.domain.model.curriculum.WorkbookSubmissionItem

interface CurriculumRepository {
    suspend fun getMyCurriculumProgress(page: Int?, limit: Int?): ApiState<StudyProgress>

    suspend fun submitChallengerWorkbook(challengerWorkbookId: Long, submission: String): ApiState<Unit>


    suspend fun getWorkbookSubmissions(
        weekNo: Int?,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int
    ): ApiState<CursorPage<WorkbookSubmissionItem>>

    suspend fun getStudyGroups(
        schoolId: Long,
        part: String
    ): ApiState<List<StudyGroup>>

    suspend fun getAvailableWeeks(): ApiState<List<Int>>

}

