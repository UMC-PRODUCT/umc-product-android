package com.umc.data.repository.workbook

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.workbook.WorkbookRepository
import com.umc.data.dataSource.remote.workbook.WorkbookRemoteDataSource
import javax.inject.Inject

class WorkbookRepositoryImpl @Inject constructor(
    private val remoteDataSource: WorkbookRemoteDataSource
) : WorkbookRepository {

    override suspend fun selectBestWorkbook(
        challengerWorkbookId: Long,
        reason: String?
    ): ApiState<Unit> =
        remoteDataSource.selectBestWorkbook(challengerWorkbookId, reason)

    override suspend fun reviewWorkbook(
        challengerWorkbookId: Long,
        status: String,
        feedback: String?
    ): ApiState<Unit> =
        remoteDataSource.reviewWorkbook(challengerWorkbookId, status, feedback)
}
