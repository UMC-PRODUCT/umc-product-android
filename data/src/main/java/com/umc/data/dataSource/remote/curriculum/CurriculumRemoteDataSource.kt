package com.umc.data.dataSource.remote.curriculum

import com.umc.data.mapper.toFailState
import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.domain.model.curriculum.StudyGroup


interface CurriculumRemoteDataSource {

    suspend fun getMyCurriculumProgress(
        page: Int?,
        limit: Int?
    ): ApiState<CurriculumProgressResponse>

    suspend fun submitChallengerWorkbook(
        challengerWorkbookId: Long,
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
    ): ApiState<List<StudyGroup>> {
        return try {
            val response = curriculumApi.getStudyGroups(schoolId, part)
            ApiState.Success(response.result.map { it.toDomain() })
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }

    suspend fun getAvailableWeeks(): ApiState<List<Int>> {
        return try {
            val response = curriculumApi.getAvailableWeeks()
            ApiState.Success(response.result.weeks)
        } catch (e: Exception) {
            ApiState.Fail(e.toFailState())
        }
    }

}
