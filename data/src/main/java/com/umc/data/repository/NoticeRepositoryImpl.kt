package com.umc.data.repository

import com.umc.data.dataSource.NoticeRemoteDataSource
import com.umc.data.response.notice.NoticeDetailResponse.Companion.toModel
import com.umc.data.response.notice.NoticeReadStatisticsResponse.Companion.toModel
import com.umc.data.response.notice.NoticeReadStatusResponse.Companion.toModel
import com.umc.data.response.notice.NoticeSearchResponse.Companion.toModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.notice.*
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeImageRequest
import com.umc.domain.model.request.notice.NoticeLinkRequest
import com.umc.domain.model.request.notice.NoticeReminderRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeRemoteDataSource: NoticeRemoteDataSource
) : NoticeRepository {

    override suspend fun deleteNotice(noticeId: Long): ApiState<Unit> =
        noticeRemoteDataSource.deleteNotice(noticeId)

    override suspend fun deleteNoticeVote(noticeId: Long): ApiState<Unit> =
        noticeRemoteDataSource.deleteNoticeVote(noticeId)

    override suspend fun getNotices(
        gisuId: Long,
        chapterId: Long?,
        schoolId: Long?,
        part: String?,
        page: Int,
        size: Int
    ): ApiState<NoticeSearch> =
        noticeRemoteDataSource.getNotices(gisuId, chapterId, schoolId, part, page, size)
            .map { it.toModel() }

    override suspend fun getNoticeDetail(noticeId: Long): ApiState<NoticeDetail> =
        noticeRemoteDataSource.getNoticeDetail(noticeId).map { it.toModel() }

    override suspend fun getNoticeReadStatus(
        noticeId: Long,
        cursorId: Long?,
        filterType: String,
        organizationIds: List<Long>?,
        status: String
    ): ApiState<NoticeReadStatus> =
        noticeRemoteDataSource.getNoticeReadStatus(
            noticeId,
            cursorId,
            filterType,
            organizationIds,
            status
        ).map { it.toModel() }

    override suspend fun getNoticeReadStatistics(noticeId: Long): ApiState<NoticeReadStatistics> =
        noticeRemoteDataSource.getNoticeReadStatistics(noticeId).map { it.toModel() }

    override suspend fun searchNotices(
        keyword: String,
        gisuId: Long,
        chapterId: Long?,
        schoolId: Long?,
        part: String?,
        page: Int,
        size: Int
    ): ApiState<NoticeSearch> =
        noticeRemoteDataSource.searchNotices(keyword, gisuId, chapterId, schoolId, part, page, size)
            .map { it.toModel() }

    override suspend fun editNoticeLinks(
        noticeId: Long,
        request: NoticeLinkRequest
    ): ApiState<Unit> =
        noticeRemoteDataSource.editNoticeLinks(noticeId, request)

    override suspend fun addNoticeImages(
        noticeId: Long,
        request: NoticeImageRequest
    ): ApiState<Unit> =
        noticeRemoteDataSource.addNoticeImages(noticeId, request)

    override suspend fun updateNoticeImages(
        noticeId: Long,
        request: NoticeUpdateRequest
    ): ApiState<Unit> =
        noticeRemoteDataSource.updateNoticeImages(noticeId, request)

    override suspend fun createNotice(request: NoticeCreateRequest): ApiState<Long> =
        noticeRemoteDataSource.createNotice(request).map { it.noticeId }

    override suspend fun addNoticeVote(noticeId: Long, request: NoticeVoteRequest): ApiState<Unit> =
        noticeRemoteDataSource.addNoticeVote(noticeId, request)

    override suspend fun sendNoticeReminder(
        noticeId: Long,
        request: NoticeReminderRequest
    ): ApiState<Unit> =
        noticeRemoteDataSource.sendNoticeReminder(noticeId, request)

    override suspend fun markNoticeAsRead(noticeId: Long): ApiState<Unit> =
        noticeRemoteDataSource.markNoticeAsRead(noticeId)
}