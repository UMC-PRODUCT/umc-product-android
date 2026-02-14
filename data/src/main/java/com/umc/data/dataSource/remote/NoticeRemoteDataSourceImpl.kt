package com.umc.data.dataSource.remote

import com.umc.data.api.NoticeApi
import com.umc.data.dataSource.NoticeRemoteDataSource
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.notice.*
import com.umc.data.response.notice.*
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class NoticeRemoteDataSourceImpl @Inject constructor(
    private val noticeApi: NoticeApi
) : NoticeRemoteDataSource {

    override suspend fun deleteNotice(noticeId: Long): ApiState<Unit> =
        apiCall { noticeApi.deleteNotice(noticeId) }

    override suspend fun deleteNoticeVote(noticeId: Long): ApiState<Unit> =
        apiCall { noticeApi.deleteNoticeVote(noticeId) }

    override suspend fun getNotices(
        gisuId: Long,
        chapterId: Long?,
        schoolId: Long?,
        part: String?,
        page: Int,
        size: Int
    ): ApiState<NoticeSearchResponse> =
        apiCall { noticeApi.getNotices(gisuId, chapterId, schoolId, part, page, size) }

    override suspend fun getNoticeDetail(noticeId: Long): ApiState<NoticeDetailResponse> =
        apiCall { noticeApi.getNoticeDetail(noticeId) }

    override suspend fun getNoticeReadStatus(
        noticeId: Long,
        cursorId: Long?,
        filterType: String,
        organizationIds: List<Long>?,
        status: String
    ): ApiState<NoticeReadStatusResponse> =
        apiCall { noticeApi.getNoticeReadStatus(noticeId, cursorId, filterType, organizationIds, status) }

    override suspend fun getNoticeReadStatistics(noticeId: Long): ApiState<NoticeReadStatisticsResponse> =
        apiCall { noticeApi.getNoticeReadStatistics(noticeId) }

    override suspend fun searchNotices(
        keyword: String,
        gisuId: Long,
        chapterId: Long?,
        schoolId: Long?,
        part: String?,
        page: Int,
        size: Int
    ): ApiState<NoticeSearchResponse> =
        apiCall { noticeApi.searchNotices(keyword, gisuId, chapterId, schoolId, part, page, size) }

    override suspend fun editNoticeLinks(noticeId: Long, request: NoticeLinkRequest): ApiState<Unit> =
        apiCall { noticeApi.editNoticeLinks(noticeId, request) }

    override suspend fun addNoticeImages(noticeId: Long, request: NoticeImageRequest): ApiState<Unit> =
        apiCall { noticeApi.addNoticeImages(noticeId, request) }

    override suspend fun updateNoticeImages(noticeId: Long, request: NoticeUpdateRequest): ApiState<Unit> =
        apiCall { noticeApi.updateNoticeImages(noticeId, request) }

    override suspend fun createNotice(request: NoticeCreateRequest): ApiState<Unit> =
        apiCall { noticeApi.createNotice(request) }

    override suspend fun addNoticeVote(noticeId: Long, request: NoticeVoteRequest): ApiState<Unit> =
        apiCall { noticeApi.addNoticeVote(noticeId, request) }

    override suspend fun sendNoticeReminder(noticeId: Long, request: NoticeReminderRequest): ApiState<Unit> =
        apiCall { noticeApi.sendNoticeReminder(noticeId, request) }

    override suspend fun markNoticeAsRead(noticeId: Long): ApiState<Unit> =
        apiCall { noticeApi.markNoticeAsRead(noticeId) }

    override suspend fun addNoticeLinks(noticeId: Long, request: NoticeLinkRequest): ApiState<Unit> =
        apiCall { noticeApi.addNoticeLinks(noticeId, request) }

    override suspend fun updateNotice(noticeId: Long, request: NoticeImageRequest): ApiState<Unit> =
        apiCall { noticeApi.updateNotice(noticeId, request) }
}