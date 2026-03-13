package com.umc.data.dataSource.remote.community

import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.request.community.CreatePostLightningRequest
import com.umc.data.request.community.CreatePostRequest
import com.umc.data.request.community.CreateTrophyRequest
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.data.response.community.CommunitySearchPostResponse
import com.umc.data.response.community.PostCommentResponse
import com.umc.data.response.community.PostDetailResponse
import com.umc.data.response.community.PostLikeResponse
import com.umc.data.response.community.PostScrapResponse
import com.umc.data.response.community.TrophyResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState

interface CommunityRemoteDataSource {

    //게시글 목록 조회
    suspend fun getPosts(category: String?, page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>

    //게시글 상세 조회
    suspend fun getPostDetail(postId: Long): ApiState<PostDetailResponse>

    //게시글 댓글 조회
    suspend fun getComments(postId: Long): ApiState<List<PostCommentResponse>>

    //게시글 댓글 작성
    suspend fun createComment(postId: Long, challengerId: Long, request: CreateCommentRequest): ApiState<PostCommentResponse>

    //게시글 검색
    suspend fun searchPosts(keyword: String, page: Int, size: Int = 20): ApiState<CommunitySearchPostResponse>

    //게시글 작성
    suspend fun createPost(challengerId: Long, request: CreatePostRequest): ApiState<PostDetailResponse>

    //번개 게시글 작성
    suspend fun createLightningPost(challengerId: Long, request: CreatePostLightningRequest): ApiState<PostDetailResponse>

    //게시글 삭제 API
    suspend fun deletePost(postId: Long): ApiState<Unit>

    //게시글 좋아요 토글
    suspend fun togglePostLike(postId: Long): ApiState<PostLikeResponse>

    //게시글 스크랩 토글
    suspend fun togglePostScrap(postId: Long): ApiState<PostScrapResponse>

    //게시글 댓글 삭제
    suspend fun deleteComment(postId: Long, commentId: Long, challengerId: Long): ApiState<Unit>

    //게시글 수정하기
    suspend fun updatePost(postId: Long, request: CreatePostRequest): ApiState<PostDetailResponse>

    //번개글 수정
    suspend fun updateLightningPost(postId: Long, request: CreatePostLightningRequest): ApiState<PostDetailResponse>

    //내 게시글 가져오기
    suspend fun getMyPosts(page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>

    //내가 댓글 단 글 가져오기
    suspend fun getMyCommentedPosts(page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>

    //내가 스크랩 한 글 가져오기
    suspend fun getMyScrappedPosts(page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>

    //게시글 신고
    suspend fun reportPost(postId: Long): ApiState<Unit>

    //댓글 신고
    suspend fun reportComment(commentId: Long): ApiState<Unit>

    //명예의 전당 게시글 생성
    suspend fun createTrophy(request: CreateTrophyRequest): ApiState<TrophyResponse>

    //명예의 전당 게시글
    suspend fun getTrophies(week: Int?, school: String?, part: String?): ApiState<List<TrophyResponse>>
}