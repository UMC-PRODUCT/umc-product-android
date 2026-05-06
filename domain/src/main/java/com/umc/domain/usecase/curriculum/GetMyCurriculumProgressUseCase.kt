package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetCurriculumOverviewUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(
        gisuId: Long,
        part: String,
        weekNo: Long? = null
    ): ApiState<StudyProgress> {
        return repository.getCurriculumOverview(gisuId, part, weekNo)
    }
}