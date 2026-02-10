package com.umc.data.dataSource.remote.curriculum

import com.umc.data.response.curriculum.CurriculumProgressResponse
import com.umc.domain.model.base.ApiState

interface CurriculumRemoteDataSource {

    suspend fun getMyCurriculumProgress(
        page: Int?,
        limit: Int?
    ): ApiState<CurriculumProgressResponse>

    suspend fun submitChallengerWorkbook(
        challengerWorkbookId: Long,
        submission: String
    ): ApiState<Unit>
}
