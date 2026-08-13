package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class UpdateMissionFeedbackUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        missionFeedbackId: Long,
        content: String,
    ): ApiState<Unit> {
        return repository.updateMissionFeedback(
            missionFeedbackId = missionFeedbackId,
            content = content,
        )
    }
}