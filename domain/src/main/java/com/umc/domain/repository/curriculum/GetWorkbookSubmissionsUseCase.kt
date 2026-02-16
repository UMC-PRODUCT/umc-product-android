package com.umc.domain.usecase.curriculum

import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetWorkbookSubmissionsUseCase @Inject constructor(
    private val repo: CurriculumRepository,
) {
    suspend operator fun invoke(
        weekNo: Int?,
        studyGroupId: Long?,
        cursor: Long?,
        size: Int = 20,
    ) = repo.getWorkbookSubmissions(weekNo, studyGroupId, cursor, size)
}
