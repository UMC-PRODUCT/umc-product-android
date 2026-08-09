package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.CurriculumOverview
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetCurriculumOverviewUseCase @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
) {
    suspend operator fun invoke(
        gisuId: Long,
        part: String,
    ): ApiState<CurriculumOverview> =
        curriculumRepository.getCurriculumOverview(
            gisuId = gisuId,
            part = part,
        )
}