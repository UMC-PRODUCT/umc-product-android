package com.umc.data.dataSource.remote.community

import com.umc.data.api.CommunityApi
import com.umc.data.dataSource.base.apiCall
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
import javax.inject.Inject

class CommunityRemoteDataSourceImpl @Inject constructor(
    private val communityApi: CommunityApi
) : CommunityRemoteDataSource {

    //게시글 목록 가져오기
    override suspend fun getPosts(
        category: String?,
        page: Int,
        size: Int
    ): ApiState<CommunityGetPostResponse> {
        return apiCall { communityApi.getPosts(category, page, size) }
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

    //게시글 검색하기
    override suspend fun searchPosts(
        keyword: String,
        page: Int,
        size: Int
    ): ApiState<CommunitySearchPostResponse> {
        return apiCall { communityApi.searchPosts(keyword, page, size) }
     }

    //일반 게시글 작성하기
    override suspend fun createPost(challengerId: Long, request: CreatePostRequest): ApiState<PostDetailResponse> {
        return apiCall {communityApi.createPost(challengerId, request)}
    }

    //번개 게시글 작성하기
    override suspend fun createLightningPost(challengerId: Long, request: CreatePostLightningRequest): ApiState<PostDetailResponse> {
        return apiCall { communityApi.createLightningPost(challengerId, request) }
    }

    //게시글 삭제하기
    override suspend fun deletePost(postId: Long): ApiState<Unit> {
        return apiCall { communityApi.deletePost(postId) }
    }

    //게시글 좋아요 토글
    override suspend fun togglePostLike(postId: Long
    ): ApiState<PostLikeResponse> {
        return apiCall { communityApi.togglePostLike(postId) }
    }

    //게시글 스크랩 토글
    override suspend fun togglePostScrap(postId: Long
    ): ApiState<PostScrapResponse> {
        return apiCall { communityApi.togglePostScrap(postId) }
    }

    //게시글 댓글 삭제하기
    override suspend fun deleteComment(postId: Long, commentId: Long, challengerId: Long
    ): ApiState<Unit> {
        return apiCall { communityApi.deleteComment(postId, commentId, challengerId) }
    }

    //게시글 수정하기
    override suspend fun updatePost(postId: Long, request: CreatePostRequest
    ): ApiState<PostDetailResponse> {
        return apiCall { communityApi.updatePost(postId, request) }
    }

    //번개글 수정하기
    override suspend fun updateLightningPost(postId: Long, request: CreatePostLightningRequest
    ): ApiState<PostDetailResponse> {
        return apiCall { communityApi.updateLightningPost(postId, request) }
    }

    //내 게시글 가저여괴
    override suspend fun getMyPosts(page: Int, size: Int): ApiState<CommunityGetPostResponse> {
        return apiCall {communityApi.getMyPosts(page, size)}
    }

    //내가 댓글단 글 가져오기
    override suspend fun getMyCommentedPosts(page: Int, size: Int
    ): ApiState<CommunityGetPostResponse> {
        return apiCall { communityApi.getMyCommentedPosts(page, size) }
    }

    //내가 스크랩한 글 가져오기
    override suspend fun getMyScrappedPosts(page: Int, size: Int
    ): ApiState<CommunityGetPostResponse> {
        return apiCall { communityApi.getMyScrappedPosts(page, size) }

    }

    //게시글 신고
    override suspend fun reportPost(postId: Long): ApiState<Unit> {
        return apiCall { communityApi.reportPost(postId) }
    }

    //댓글 신고
    override suspend fun reportComment(commentId: Long): ApiState<Unit> {
        return apiCall { communityApi.reportComment(commentId) }
    }

    //명예의 전당 가져오기
    override suspend fun getTrophies(
        week: Int?,
        school: String?,
        part: String?
    ): ApiState<List<TrophyResponse>> {
        return apiCall { communityApi.getTrophies(week, school, part) }
    }

    //명예의 전당 게시글 작성
    override suspend fun createTrophy(request: CreateTrophyRequest): ApiState<TrophyResponse> {
        return apiCall { communityApi.createTrophy(request) }
    }

}