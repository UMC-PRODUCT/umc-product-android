package com.umc.data.dataSource

import com.umc.data.request.notice.NoticeCreateRequest
import com.umc.data.request.notice.NoticeImageRequest
import com.umc.data.request.notice.NoticeLinkRequest
import com.umc.data.request.notice.NoticeReminderRequest
import com.umc.data.request.notice.NoticeUpdateRequest
import com.umc.data.request.notice.NoticeVoteRequest
import com.umc.data.response.notice.NoticeDetailResponse
import com.umc.data.response.notice.NoticeReadStatisticsResponse
import com.umc.data.response.notice.NoticeReadStatusResponse
import com.umc.data.response.notice.NoticeSearchResponse
import com.umc.domain.model.base.ApiState

interface NoticeRemoteDataSource {
    suspend fun deleteNotice(noticeId: Long): ApiState<Unit>
    suspend fun deleteNoticeVote(noticeId: Long): ApiState<Unit>
    suspend fun getNotices(
        gisuId: Long,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiState<NoticeSearchResponse>

    suspend fun getNoticeDetail(noticeId: Long): ApiState<NoticeDetailResponse>
    suspend fun getNoticeReadStatus(
        noticeId: Long,
        cursorId: Long? = null,
        filterType: String,
        organizationIds: List<Long>? = null,
        status: String
    ): ApiState<NoticeReadStatusResponse>

    suspend fun getNoticeReadStatistics(noticeId: Long): ApiState<NoticeReadStatisticsResponse>
    suspend fun searchNotices(
        keyword: String = "",
        gisuId: Long,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiState<NoticeSearchResponse>

    suspend fun editNoticeLinks(noticeId: Long, request: NoticeLinkRequest): ApiState<Unit>
    suspend fun addNoticeImages(noticeId: Long, request: NoticeImageRequest): ApiState<Unit>
    suspend fun updateNoticeImages(noticeId: Long, request: NoticeUpdateRequest): ApiState<Unit>
    suspend fun createNotice(request: NoticeCreateRequest): ApiState<Unit>
    suspend fun addNoticeVote(noticeId: Long, request: NoticeVoteRequest): ApiState<Unit>
    suspend fun sendNoticeReminder(noticeId: Long, request: NoticeReminderRequest): ApiState<Unit>
    suspend fun markNoticeAsRead(noticeId: Long): ApiState<Unit>
    suspend fun addNoticeLinks(noticeId: Long, request: NoticeLinkRequest): ApiState<Unit>
    suspend fun updateNotice(noticeId: Long, request: NoticeImageRequest): ApiState<Unit>
}