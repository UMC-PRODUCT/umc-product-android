package com.umc.presentation.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoticePaginationTest {
    @Test
    fun removesDuplicatesWithinAndAcrossPagesWithoutReorderingExistingNotices() = runBlocking {
        val existing = listOf(notice(2), notice(1))
        val result = loadUniqueNoticePage(existing, firstPage = 1) { page ->
            assertEquals(1, page)
            success(page, true, notice(1, "overlap"), notice(4), notice(4, "duplicate"), notice(3))
        }

        assertEquals(1, result.pageNumber)
        assertEquals(existing + listOf(notice(4), notice(3)), result.content)
    }

    @Test
    fun skipsConsecutivePagesWithoutNewNotices() = runBlocking {
        val requested = mutableListOf<Int>()
        val result = loadUniqueNoticePage(listOf(notice(1)), firstPage = 1) { page ->
            requested += page
            when (page) {
                1 -> success(page, true, notice(1))
                2 -> success(page, true)
                3 -> success(page, true, notice(2))
                else -> error("Unexpected page: $page")
            }
        }

        assertEquals(listOf(1, 2, 3), requested)
        assertEquals(3, result.pageNumber)
        assertEquals(listOf(notice(1), notice(2)), result.content)
    }

    @Test
    fun stopsAtLastPageEvenWhenAllNoticesOverlap() = runBlocking {
        val existing = listOf(notice(1))
        val result = loadUniqueNoticePage(existing, firstPage = 2) { page ->
            assertEquals(2, page)
            success(page, false, notice(1))
        }

        val response = (result.response as ApiState.Success).data
        assertEquals(existing, response.content)
        assertFalse(response.hasNext)
    }

    @Test
    fun reportsTheFailedPageSoRetryCanResumeThere() = runBlocking {
        val existing = listOf(notice(1))
        val failure = ApiState.Fail(FailState(code = "NETWORK_ERROR", message = "offline"))
        val failed = loadUniqueNoticePage(existing, firstPage = 1) { page ->
            if (page == 1) success(page, true, notice(1)) else failure
        }

        assertEquals(2, failed.pageNumber)
        assertEquals(failure, failed.response)

        val retried = loadUniqueNoticePage(existing, firstPage = failed.pageNumber) { page ->
            assertEquals(2, page)
            success(page, false, notice(1), notice(2))
        }
        assertEquals(listOf(notice(1), notice(2)), retried.content)
    }

    @Test
    fun firstLoadSkipsAnEmptyPageAndDeduplicatesTheNextPage() = runBlocking {
        val result = loadUniqueNoticePage(emptyList(), firstPage = 0) { page ->
            when (page) {
                0 -> success(page, true)
                1 -> success(page, false, notice(7), notice(7, "duplicate"))
                else -> error("Unexpected page: $page")
            }
        }

        assertEquals(1, result.pageNumber)
        assertEquals(listOf(notice(7)), result.content)
    }

    @Test
    fun cancelledRequestDoesNotPublishALateResponse() = runBlocking {
        var resultPublished = false
        val requested = mutableListOf<Int>()
        val job = launch {
            loadUniqueNoticePage(listOf(notice(1)), firstPage = 1) { page ->
                requested += page
                if (page == 2) currentCoroutineContext().cancel()
                success(page, true, notice(if (page == 1) 1 else 2))
            }
            resultPublished = true
        }

        job.join()

        assertTrue(job.isCancelled)
        assertEquals(listOf(1, 2), requested)
        assertFalse(resultPublished)
    }

    private val NoticePageLoad.content: List<NoticeSummary>
        get() = (response as ApiState.Success).data.content

    private fun success(page: Int, hasNext: Boolean, vararg notices: NoticeSummary) =
        ApiState.Success(
            NoticeSearch(
                content = notices.toList(),
                page = page,
                size = 20,
                totalElements = 80,
                totalPages = 4,
                hasNext = hasNext,
                hasPrevious = page > 0,
            ),
        )

    private fun notice(id: Long, title: String = "Notice $id") = NoticeSummary(
        id = id,
        title = title,
        content = "",
        shouldSendNotification = false,
        viewCount = 0,
        createdAt = "",
        targetInfo = NoticeTarget(),
        authorChallengerId = 1,
        authorNickname = "",
        authorName = "",
    )
}
