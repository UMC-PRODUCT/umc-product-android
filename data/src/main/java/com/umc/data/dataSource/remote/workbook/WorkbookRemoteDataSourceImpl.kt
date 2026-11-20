package com.umc.data.dataSource.remote.workbook

import com.umc.data.api.WorkbookApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.dataSource.base.requireResult
import com.umc.data.request.workbook.BestWorkbookRequest
import com.umc.data.request.workbook.ReviewWorkbookRequest
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.base.mapCatching
import com.umc.domain.model.curriculum.ChallengerWorkbookSubmission
import javax.inject.Inject

/**
 * apiCall 로 감싸 success=false 응답을 실패로 보고, 서버 오류 본문과 네트워크 단절을 다른 API 와 같게 분류한다.
 * 취소는 apiCall 이 다시 던진다.
 */
class WorkbookRemoteDataSourceImpl @Inject constructor(
    private val api: WorkbookApi
) : WorkbookRemoteDataSource {

    override suspend fun selectBestWorkbook(
        challengerWorkbookId: Long,
        reason: String?
    ): ApiState<Unit> =
        apiCall {
            api.selectBestWorkbook(
                challengerWorkbookId = challengerWorkbookId,
                body = BestWorkbookRequest(bestReason = reason)
            )
        }.map { }

    override suspend fun reviewWorkbook(
        challengerWorkbookId: Long,
        status: String,
        feedback: String?
    ): ApiState<Unit> =
        apiCall {
            api.reviewWorkbook(
                challengerWorkbookId = challengerWorkbookId,
                body = ReviewWorkbookRequest(status = status, feedback = feedback)
            )
        }.map { }

    override suspend fun getChallengerWorkbookSubmission(
        challengerWorkbookId: Long
    ): ApiState<ChallengerWorkbookSubmission> {
        // result 가 비어 오거나 화면 모델로 바꾸다 실패하면 앱을 종료하지 않고 실패로 돌린다.
        return apiCall { api.getChallengerWorkbookSubmission(challengerWorkbookId) }
            .requireResult()
            .mapCatching { it.toModel() }
    }
}
