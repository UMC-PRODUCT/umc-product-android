package com.umc.presentation.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.model.notice.NoticeSummary
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal data class NoticePageLoad(
    val pageNumber: Int,
    val response: ApiState<NoticeSearch>,
)

/**
 * 기존 목록의 순서를 유지하고 추가할 공지의 ID 중복을 제거하며, 새 공지가 있는 페이지나 마지막 페이지까지 조회한다.
 * 성공 시 합쳐진 목록을 반환한다. 실패 시 [NoticePageLoad.pageNumber]는 재시도할 실패 페이지다.
 */
internal suspend fun loadUniqueNoticePage(
    existing: List<NoticeSummary>,
    firstPage: Int,
    fetchPage: suspend (Int) -> ApiState<NoticeSearch>,
): NoticePageLoad {
    val knownIds = existing.mapTo(HashSet()) { it.id }
    var pageNumber = firstPage

    while (true) {
        val response = fetchPage(pageNumber)
        // 서버 호출이 취소 직전에 반환해도 이전 조건의 결과를 상태에 반영하지 않는다.
        currentCoroutineContext().ensureActive()

        when (response) {
            is ApiState.Fail -> return NoticePageLoad(pageNumber, response)
            is ApiState.Success -> {
                val page = response.data
                val newNotices = page.content.filter { knownIds.add(it.id) }
                if (newNotices.isNotEmpty() || !page.hasNext) {
                    return NoticePageLoad(pageNumber, ApiState.Success(page.copy(content = existing + newNotices)))
                }
                pageNumber++
            }
        }
    }
}
