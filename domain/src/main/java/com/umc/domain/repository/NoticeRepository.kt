package com.umc.domain.repository

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeReadStatus
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeImageRequest
import com.umc.domain.model.request.notice.NoticeLinkRequest
import com.umc.domain.model.request.notice.NoticeReminderRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.model.request.survey.VoteResponseRequest

interface NoticeRepository {
    suspend fun deleteNotice(noticeId: Long): ApiState<Unit>
    suspend fun deleteNoticeVote(noticeId: Long): ApiState<Unit>
    suspend fun getNotices(
        gisuId: Long,
        noticeTab: String,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiState<NoticeSearch>

    suspend fun getNoticeDetail(noticeId: Long): ApiState<NoticeDetail>
    suspend fun getNoticeReadStatus(
        noticeId: Long,
        cursorId: Long? = null,
        filterType: String,
        organizationIds: List<Long>? = null,
        status: String
    ): ApiState<NoticeReadStatus>

    suspend fun getNoticeReadStatistics(noticeId: Long): ApiState<NoticeReadStatistics>
    suspend fun searchNotices(
        keyword: String = "",
        gisuId: Long,
        noticeTab: String,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiState<NoticeSearch>

    suspend fun editNoticeLinks(noticeId: Long, request: NoticeLinkRequest): ApiState<Unit>
    suspend fun addNoticeImages(noticeId: Long, request: NoticeImageRequest): ApiState<Unit>
    suspend fun updateNoticeImages(noticeId: Long, request: NoticeImageRequest): ApiState<Unit>
    suspend fun updateNotice(noticeId: Long, request: NoticeUpdateRequest): ApiState<Unit>
    suspend fun createNotice(request: NoticeCreateRequest): ApiState<Long>
    suspend fun addNoticeVote(noticeId: Long, request: NoticeVoteRequest): ApiState<Unit>
    suspend fun sendNoticeReminder(noticeId: Long, request: NoticeReminderRequest): ApiState<Unit>
    suspend fun markNoticeAsRead(noticeId: Long): ApiState<Unit>
    suspend fun submitVoteResponse(voteId: Long, request: VoteResponseRequest): ApiState<Unit>
    suspend fun updateVoteResponse(voteId: Long, request: VoteResponseRequest): ApiState<Unit>
}