package com.umc.data.api

import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.request.community.CreatePostLightningRequest
import com.umc.data.request.community.CreatePostRequest
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.data.response.community.CommunitySearchPostResponse
import com.umc.data.response.community.PostCommentResponse
import com.umc.data.response.community.PostDetailResponse
import com.umc.data.response.community.PostLikeResponse
import com.umc.data.response.community.PostScrapResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {

    //게시글 목록 조회
    @GET(Endpoints.Community.COMMUNITY)
    suspend fun getPosts(
        @Query("category") category: String?,
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


    //게시글 검색
    @GET(Endpoints.Community.POST_SEARCH)
    suspend fun searchPosts(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int = 20
    ) : ApiResponse<CommunitySearchPostResponse>

    //일반 게시글 작성
    @POST(Endpoints.Community.COMMUNITY)
    suspend fun createPost(
        @Query("challengerId") challengerId: Long,
        @Body request: CreatePostRequest
    ): ApiResponse<PostDetailResponse>

    //번개 게시글 작성
    @POST(Endpoints.Community.LIGHTNING)
    suspend fun createLightningPost(
        @Query("challengerId") challengerId: Long,
        @Body request: CreatePostLightningRequest
    ): ApiResponse<PostDetailResponse>

    //게시글 삭제
    @DELETE(Endpoints.Community.POST_DETAIL)
    suspend fun deletePost(
        @Path("postId") postId: Long
    ): ApiResponse<Unit>

    //게시글 좋아요 토글
    @POST(Endpoints.Community.POST_LIKE)
    suspend fun togglePostLike(
        @Path("postId") postId: Long,
        //@Query("challengerId") challengerId: Long
    ): ApiResponse<PostLikeResponse>

    //게시글 스크랩 토글
    @POST(Endpoints.Community.POST_SCRAP)
    suspend fun togglePostScrap(
        @Path("postId") postId: Long,
        //@Query("challengerId") challengerId: Long
    ): ApiResponse<PostScrapResponse>



    //게시글 댓글 삭제
    @DELETE(Endpoints.Community.POST_COMMENT_DETAIL)
    suspend fun deleteComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Query("challengerId") challengerId: Long
    ) : ApiResponse<Unit>

    //게시글 수정
    @PATCH(Endpoints.Community.POST_DETAIL)
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body request: CreatePostRequest
    ): ApiResponse<PostDetailResponse>


    //내가 쓴 글 조회
    @GET(Endpoints.Community.MY_POST)
    suspend fun getMyPosts(
        @Query("page") page: Int, // 현재 페이지
        @Query("size") size: Int = 20 // 페이지당 개수
    ): ApiResponse<CommunityGetPostResponse>

    //내가 댓글 단 글 조회
    @GET(Endpoints.Community.MY_COMMENT)
    suspend fun getMyCommentedPosts(
        @Query("page") page: Int, // 현재 페이지
        @Query("size") size: Int = 20 // 페이지당 개수
    ): ApiResponse<CommunityGetPostResponse>

    //내가 스크랩 한 글 조회
    @GET(Endpoints.Community.MY_SCRAP)
    suspend fun getMyScrappedPosts(
        @Query("page") page: Int, // 현재 페이지
        @Query("size") size: Int = 20 // 페이지당 개수
    ): ApiResponse<CommunityGetPostResponse>

}