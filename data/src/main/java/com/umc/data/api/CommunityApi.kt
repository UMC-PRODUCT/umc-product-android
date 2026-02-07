package com.umc.data.api

import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.data.response.community.PostCommentResponse
import com.umc.data.response.community.PostDetailResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {

    //게시글 목록 조회
    @GET(Endpoints.Community.COMMUNITY)
    suspend fun getPosts(
        @Query("ing") ing: Boolean = false, // 모집 중인 번개 게시글글만 조회
        @Query("sort") sort: String = "ALL", // 정렬 기준 (SOFT:좋아요, HARD:좋아요역순, ALL:최신순)
        @Query("page") page: Int, // 현재 페이지
        @Query("size") size: Int = 20 // 페이지당 개수
    ): ApiResponse<CommunityGetPostResponse>

    //게시글 상세 조회
    @GET(Endpoints.Community.POST_DETAIL)
    suspend fun getPostDetail(
        @Path("postId") postId: Long
    ): ApiResponse<PostDetailResponse>

    //게시글 댓글 목록 조회
    @GET(Endpoints.Community.POST_COMMENT)
    suspend fun getComments(
        @Path("postId") postId: Long
    ): ApiResponse<List<PostCommentResponse>>

    //게시글 댓글 작성
    @POST(Endpoints.Community.POST_COMMENT)
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Query("challengerId") challengerId: Long,
        @Body request: CreateCommentRequest
    ): ApiResponse<PostCommentResponse>

}