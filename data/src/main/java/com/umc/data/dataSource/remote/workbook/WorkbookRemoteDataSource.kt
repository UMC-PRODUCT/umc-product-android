package com.umc.data.dataSource.remote.workbook

import com.umc.domain.model.base.ApiState

interface WorkbookRemoteDataSource {

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
