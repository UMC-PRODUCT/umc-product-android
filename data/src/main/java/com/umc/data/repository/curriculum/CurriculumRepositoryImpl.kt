package com.umc.data.repository.curriculum

import com.umc.data.dataSource.remote.curriculum.CurriculumRemoteDataSource
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val remote: CurriculumRemoteDataSource
) : CurriculumRepository {

    override suspend fun getMyCurriculumProgress(
        page: Int?,
        limit: Int?
    ): ApiState<StudyProgress> {
        return when (val res = remote.getMyCurriculumProgress(page, limit)) {
            is ApiState.Success -> ApiState.Success(res.data.toModel())
            is ApiState.Fail -> res
        }
    }


    override suspend fun submitChallengerWorkbook(
        challengerWorkbookId: Long,
        submission: String
    ) = remote.submitChallengerWorkbook(challengerWorkbookId, submission)
}
