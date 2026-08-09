package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetWorkbookSubmissionWeeksUseCase @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
) {
    suspend operator fun invoke(
        studyGroupId: Long? = null,
    ): ApiState<List<Long>> {
        return curriculumRepository.getWorkbookSubmissionWeeks(
            studyGroupId = studyGroupId,
        )
    }
}