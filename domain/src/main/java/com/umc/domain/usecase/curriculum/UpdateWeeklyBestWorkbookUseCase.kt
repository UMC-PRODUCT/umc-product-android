package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class UpdateWeeklyBestWorkbookUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        weeklyBestWorkbookId: Long,
        reason: String,
    ): ApiState<Unit> {
        return repository.updateWeeklyBestWorkbook(
            weeklyBestWorkbookId = weeklyBestWorkbookId,
            reason = reason,
        )
    }
}