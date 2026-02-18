package com.umc.domain.usecase.workbook

import com.umc.domain.repository.workbook.WorkbookRepository
import javax.inject.Inject


class SelectBestWorkbookUseCase @Inject constructor(
    private val repository: WorkbookRepository
) {
    suspend operator fun invoke(
        challengerWorkbookId: Long,
        reason: String?
    ) = repository.selectBestWorkbook(challengerWorkbookId, reason)
}
