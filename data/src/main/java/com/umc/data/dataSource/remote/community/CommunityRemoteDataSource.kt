package com.umc.data.dataSource.remote.community

import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.data.response.community.PostCommentResponse
import com.umc.data.response.community.PostDetailResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState

interface CommunityRemoteDataSource {

    //게시글 목록 조회
    suspend fun getPosts(ing: Boolean = false, sort: String = "ALL",
                         page: Int, size: Int = 20): ApiState<CommunityGetPostResponse>

    //게시글 상세 조회
    suspend fun getPostDetail(postId: Long): ApiState<PostDetailResponse>

    //게시글 댓글 조회
    suspend fun getComments(postId: Long): ApiState<List<PostCommentResponse>>

    //게시글 댓글 작성
    suspend fun createComment(postId: Long, challengerId: Long, request: CreateCommentRequest): ApiState<PostCommentResponse>




}