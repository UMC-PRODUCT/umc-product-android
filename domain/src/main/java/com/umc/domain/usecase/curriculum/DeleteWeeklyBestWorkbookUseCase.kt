package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class DeleteWeeklyBestWorkbookUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        weeklyBestWorkbookId: Long,
    ): ApiState<Unit> {
        return repository.deleteWeeklyBestWorkbook(
            weeklyBestWorkbookId = weeklyBestWorkbookId,
        )
    }
}