package com.umc.domain.usecase.curriculum

import com.umc.domain.repository.curriculum.CurriculumRepository
import jakarta.inject.Inject

class GetStudyGroupsUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(
        schoolId: Long,
        part: String
    ) = repository.getStudyGroups(schoolId, part)
}
