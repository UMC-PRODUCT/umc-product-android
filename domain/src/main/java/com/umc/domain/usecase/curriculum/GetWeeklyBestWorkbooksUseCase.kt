package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.WeeklyBestWorkbookPage
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetWeeklyBestWorkbooksUseCase @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
) {
    suspend operator fun invoke(
        gisuId: Long? = null,
        schoolIds: List<Long>? = null,
        parts: List<String>? = null,
        weekNos: List<Long>? = null,
        studyGroupIds: List<Long>? = null,
        page: Int = 0,
        size: Int = 20,
    ): ApiState<WeeklyBestWorkbookPage> {
        return curriculumRepository.getWeeklyBestWorkbooks(
            gisuId = gisuId,
            schoolIds = schoolIds,
            parts = parts,
            weekNos = weekNos,
            studyGroupIds = studyGroupIds,
            page = page,
            size = size,
        )
    }
}