package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class CreateMissionFeedbackUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        missionSubmissionId: Long,
        content: String,
        result: String,
    ): ApiState<Unit> {
        return repository.createMissionFeedback(
            missionSubmissionId = missionSubmissionId,
            content = content,
            result = result,
        )
    }
}