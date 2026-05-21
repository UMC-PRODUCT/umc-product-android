package com.umc.data.dataSource.remote.curriculum

import com.umc.data.mapper.toFailState
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.CurriculumOverviewResponse
import com.umc.domain.model.curriculum.StudyGroup


interface CurriculumRemoteDataSource {


    suspend fun getCurriculumOverview(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverviewResponse>

    suspend fun getMyCurriculumProgress(
        gisuId: Long
    ): ApiState<CurriculumProgressResponse>

    suspend fun submitChallengerWorkbook(
        originalWorkbookId: Long,
        submission: String
    ): ApiState<Unit>

    suspend fun getWorkbookSubmissions(
        weekNo: Int,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int,
    ): ApiState<WorkbookSubmissionsResponse>


    suspend fun getStudyGroups(
        schoolId: Long,
        part: String
    ): ApiState<List<StudyGroup>>

    suspend fun getAvailableWeeks(): ApiState<List<Int>>
}
