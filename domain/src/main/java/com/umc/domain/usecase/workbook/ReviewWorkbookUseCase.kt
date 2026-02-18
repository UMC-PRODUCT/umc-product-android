package com.umc.domain.usecase.workbook

import com.umc.domain.repository.workbook.WorkbookRepository
import javax.inject.Inject


class ReviewWorkbookUseCase @Inject constructor(
    private val repository: WorkbookRepository
) {
    suspend operator fun invoke(
        challengerWorkbookId: Long,
        status: String,
        feedback: String?
    ) = repository.reviewWorkbook(challengerWorkbookId, status, feedback)
}
