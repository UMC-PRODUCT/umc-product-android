package com.umc.domain.usecase.curriculum

import com.umc.domain.repository.curriculum.CurriculumRepository
import jakarta.inject.Inject

class GetAvailableWeeksUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke()
            = repository.getAvailableWeeks()
}
