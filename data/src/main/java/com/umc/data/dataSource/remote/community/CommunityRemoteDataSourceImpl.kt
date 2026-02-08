package com.umc.data.dataSource.remote.community

import com.umc.data.api.CommunityApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.response.community.CommunityGetPostResponse
import com.umc.data.response.community.PostCommentResponse
import com.umc.data.response.community.PostDetailResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class CommunityRemoteDataSourceImpl @Inject constructor(
    private val communityApi: CommunityApi
) : CommunityRemoteDataSource {

    //게시글 목록 가져오기
    override suspend fun getPosts(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
    ): ApiState<CommunityGetPostResponse> {
        return apiCall { communityApi.getPosts(ing, sort, page, size) }
    }

    //게시글 상세 내용 가져오기
    override suspend fun getPostDetail(postId: Long): ApiState<PostDetailResponse> {
        return apiCall { communityApi.getPostDetail(postId) }
    }

    //댓글 목록 가져오기
    override suspend fun getComments(postId: Long): ApiState<List<PostCommentResponse>> {
        return apiCall { communityApi.getComments(postId) }
    }

    //댓글 작성하기
    override suspend fun createComment(
        postId: Long,
        challengerId: Long,
        request: CreateCommentRequest
    ): ApiState<PostCommentResponse> {
        return apiCall { communityApi.createComment(postId, challengerId, request) }

    }
}