package com.umc.domain.repository.workbook

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.ChallengerWorkbookSubmission

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

    suspend fun getChallengerWorkbookSubmission(
        challengerWorkbookId: Long
    ): ApiState<ChallengerWorkbookSubmission>
}
