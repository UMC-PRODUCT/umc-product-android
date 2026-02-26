package com.umc.data.api

import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.model.request.notice.NoticeImageRequest
import com.umc.domain.model.request.notice.NoticeLinkRequest
import com.umc.domain.model.request.notice.NoticeReminderRequest
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.model.request.survey.VoteResponseRequest
import com.umc.data.response.notice.NoticeCreateResponse
import com.umc.data.response.notice.NoticeDetailResponse
import com.umc.data.response.notice.NoticeReadStatisticsResponse
import com.umc.data.response.notice.NoticeReadStatusResponse
import com.umc.data.response.notice.NoticeSearchResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NoticeApi {

    companion object {
        const val PATH_NOTICE_ID = "noticeId"
        const val PATH_VOTE_ID = "voteId"
        const val QUERY_GISU_ID = "gisuId"
        const val QUERY_CHAPTER_ID = "chapterId"
        const val QUERY_SCHOOL_ID = "schoolId"
        const val QUERY_PART = "part"
        const val QUERY_PAGE = "page"
        const val QUERY_SIZE = "size"
        const val QUERY_CURSOR_ID = "cursorId"
        const val QUERY_FILTER_TYPE = "filterType"
        const val QUERY_ORGANIZATION_IDS = "organizationIds"
        const val QUERY_STATUS = "status"
        const val QUERY_KEYWORD = "keyword"
    }

    // 공지사항 삭제
    @DELETE(Endpoints.Notice.NOTICE_DETAIL)
    suspend fun deleteNotice(
        @Path(PATH_NOTICE_ID) noticeId: Long
    ): ApiResponse<Unit>


    // 공지사항 투표 삭제
    @DELETE(Endpoints.Notice.NOTICE_VOTE)
    suspend fun deleteNoticeVote(
        @Path(PATH_NOTICE_ID) noticeId: Long
    ): ApiResponse<Unit>

    // TODO 공지 리스트
    @GET(Endpoints.Notice.NOTICE)
    suspend fun getNotices(
        @Query(QUERY_GISU_ID) gisuId: Long,
        @Query(QUERY_CHAPTER_ID) chapterId: Long? = null,
        @Query(QUERY_SCHOOL_ID) schoolId: Long? = null,
        @Query(QUERY_PART) part: String? = null,
        @Query(QUERY_PAGE) page: Int = 0,
        @Query(QUERY_SIZE) size: Int = 10,
    ): ApiResponse<NoticeSearchResponse>

    // 공지사항 상세 조회
    @GET(Endpoints.Notice.NOTICE_DETAIL)
    suspend fun getNoticeDetail(
        @Path(PATH_NOTICE_ID) noticeId: Long
    ): ApiResponse<NoticeDetailResponse>

    // 공지사항 읽음 현황 상세 조회
    @GET(Endpoints.Notice.NOTICE_READ_STATUS)
    suspend fun getNoticeReadStatus(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Query(QUERY_CURSOR_ID) cursorId: Long? = null,
        @Query(QUERY_FILTER_TYPE) filterType: String,
        @Query(QUERY_ORGANIZATION_IDS) organizationIds: List<Long>? = null,
        @Query(QUERY_STATUS) status: String
    ): ApiResponse<NoticeReadStatusResponse>

    // 공지사항 읽음 통계 조회
    @GET(Endpoints.Notice.NOTICE_READ_STATICS)
    suspend fun getNoticeReadStatistics(
        @Path(PATH_NOTICE_ID) noticeId: Long
    ): ApiResponse<NoticeReadStatisticsResponse>

    // 공지사항 검색
    @GET(Endpoints.Notice.NOTICE_SEARCH)
    suspend fun searchNotices(
        @Query(QUERY_KEYWORD) keyword: String = "",
        @Query(QUERY_GISU_ID) gisuId: Long,
        @Query(QUERY_CHAPTER_ID) chapterId: Long? = null,
        @Query(QUERY_SCHOOL_ID) schoolId: Long? = null,
        @Query(QUERY_PART) part: String? = null,
        @Query(QUERY_PAGE) page: Int = 0,
        @Query(QUERY_SIZE) size: Int = 10,
    ): ApiResponse<NoticeSearchResponse>

    // 공지사항 링크 추가/수정
    @PATCH(Endpoints.Notice.NOTICE_LINKS)
    suspend fun editNoticeLinks(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeLinkRequest,
    ): ApiResponse<Unit>

    // 공지사항 이미지 추가/수정
    @PATCH(Endpoints.Notice.NOTICE_IMAGES)
    suspend fun addNoticeImages(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeImageRequest
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Notice.NOTICE_IMAGES)
    suspend fun updateNoticeImages(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeUpdateRequest
    ): ApiResponse<Unit>

    // 공지사항 기본 정보 수정
    @PATCH(Endpoints.Notice.NOTICE_DETAIL)
    suspend fun updateNotice(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeUpdateRequest
    ): ApiResponse<Unit>

    // 공지사항 생성
    @POST(Endpoints.Notice.NOTICE)
    suspend fun createNotice(
        @Body request: NoticeCreateRequest
    ): ApiResponse<NoticeCreateResponse>

    // 공지사항 투표 추가
    @POST(Endpoints.Notice.NOTICE_VOTES)
    suspend fun addNoticeVote(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeVoteRequest
    ): ApiResponse<Unit>

    // 공지사항 리마인더 발송
    @POST(Endpoints.Notice.NOTICE_REMINDERS)
    suspend fun sendNoticeReminder(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeReminderRequest
    ): ApiResponse<Unit>

    // 공지사항 읽음 처리
    @POST(Endpoints.Notice.NOTICE_READ)
    suspend fun markNoticeAsRead(
        @Path(PATH_NOTICE_ID) noticeId: Long
    ): ApiResponse<Unit>

    // 공지사항 링크 추가
    @POST(Endpoints.Notice.NOTICE_LINKS)
    suspend fun addNoticeLinks(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeLinkRequest,
    ): ApiResponse<Unit>

    // 공지사항 이미지 추가
    @POST(Endpoints.Notice.NOTICE_IMAGES)
    suspend fun updateNotice(
        @Path(PATH_NOTICE_ID) noticeId: Long,
        @Body request: NoticeImageRequest
    ): ApiResponse<Unit>

    // 투표 응답 제출
    @POST(Endpoints.Survey.VOTE_RESPONSES)
    suspend fun submitVoteResponse(
        @Path(PATH_VOTE_ID) voteId: Long,
        @Body request: VoteResponseRequest
    ): ApiResponse<Unit>

}