package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class CreateWeeklyBestWorkbookUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        bestMemberId: Long,
        weeklyCurriculumId: Long,
        studyGroupId: Long,
        reason: String,
    ): ApiState<Unit> {
        return repository.createWeeklyBestWorkbook(
            bestMemberId = bestMemberId,
            weeklyCurriculumId = weeklyCurriculumId,
            studyGroupId = studyGroupId,
            reason = reason,
        )
    }
}