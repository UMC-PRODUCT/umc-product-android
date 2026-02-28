package com.umc.domain.usecase.workbook

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.ChallengerWorkbookSubmission
import com.umc.domain.repository.workbook.WorkbookRepository
import javax.inject.Inject

class GetChallengerWorkbookSubmissionUseCase @Inject constructor(
    private val repo: WorkbookRepository
) {
    suspend operator fun invoke(challengerWorkbookId: Long): ApiState<ChallengerWorkbookSubmission> {
        return repo.getChallengerWorkbookSubmission(challengerWorkbookId)
    }
}