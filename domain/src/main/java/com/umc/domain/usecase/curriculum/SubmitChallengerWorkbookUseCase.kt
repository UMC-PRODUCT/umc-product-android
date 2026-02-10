package com.umc.domain.usecase.curriculum

import com.umc.domain.repository.curriculum.CurriculumRepository
import jakarta.inject.Inject

class SubmitChallengerWorkbookUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(
        challengerWorkbookId: Long,
        submission: String
    ) = repository.submitChallengerWorkbook(challengerWorkbookId, submission)
}
