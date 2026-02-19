package com.umc.domain.repository.workbook

import com.umc.domain.model.base.ApiState

interface WorkbookRepository {

    suspend fun selectBestWorkbook(
        challengerWorkbookId: Long,
        reason: String?
    ): ApiState<Unit>

    suspend fun reviewWorkbook(
        challengerWorkbookId: Long,
        status: String,
        feedback: String?
    ): ApiState<Unit>
}
