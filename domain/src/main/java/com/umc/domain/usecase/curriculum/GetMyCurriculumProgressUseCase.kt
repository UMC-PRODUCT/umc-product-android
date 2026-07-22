package com.umc.domain.usecase.curriculum

import com.umc.domain.model.act.study.StudyProgress
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetMyCurriculumProgressUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        gisuId: Long,
    ): ApiState<StudyProgress> {
        return repository.getMyCurriculumProgress(
            gisuId = gisuId,
        )
    }
}