package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.ChallengerWorkbook
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetChallengerWorkbookDetailUseCase @Inject constructor(
    private val repository: CurriculumRepository,
) {
    suspend operator fun invoke(
        challengerWorkbookId: Long,
    ): ApiState<ChallengerWorkbook> {
        return repository.getChallengerWorkbookDetail(
            challengerWorkbookId = challengerWorkbookId,
        )
    }
}