package com.umc.data.repository.community

import com.umc.data.dataSource.remote.community.CommunityRemoteDataSource
import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.request.community.CreatePostLightningRequest
import com.umc.data.request.community.CreatePostRequest
import com.umc.data.response.community.CommunityGetPostResponse.Companion.toPostPageModelDomain
import com.umc.data.response.community.CommunitySearchPostResponse.Companion.toPostPageModelDomain
import com.umc.data.response.community.PostCommentResponse.Companion.toCommentItemDomain
import com.umc.data.response.community.PostDetailResponse.Companion.toContentItemDomain
import com.umc.data.response.community.PostLikeResponse.Companion.toPostLikeDomain
import com.umc.data.response.community.PostScrapResponse.Companion.toPostScrapDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.model.community.CreatePost
import com.umc.domain.model.community.PostLike
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.model.community.PostScrap
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityRemoteDataSource: CommunityRemoteDataSource
) : CommunityRepository {

    //게시글 작성
    override suspend fun getPosts(
        category: String?,
        page: Int,
        size: Int
    ): ApiState<PostPageModel> {
        return communityRemoteDataSource.getPosts(category, page, size).map {
            it.toPostPageModelDomain() }
    }

    //게시글 상세
    override suspend fun getPostDetail(postId: Long): ApiState<ContentItem> {
        return communityRemoteDataSource.getPostDetail(postId).map {
            it.toContentItemDomain() }
    }

    //게시글 댓글 조회
    override suspend fun getComments(postId: Long): ApiState<List<CommentItem>> {
        return communityRemoteDataSource.getComments(postId).map {
            it.map { it.toCommentItemDomain() } }
    }

    //게시글 댓글 작성
    override suspend fun createComment(
        postId: Long,
        challengerId: Long,
        content: String,
        parentId: Long
    ): ApiState<CommentItem> {
        val request = CreateCommentRequest(content, parentId)
        return communityRemoteDataSource.createComment(postId, challengerId, request).map {
            it.toCommentItemDomain() }

    }

    //게시글 검색
    override suspend fun searchPosts(
        keyword: String,
        page: Int,
        size: Int
    ): ApiState<PostPageModel> {
        return communityRemoteDataSource.searchPosts(keyword, page, size).map {
            it.toPostPageModelDomain() }
    }

    //일반게시글 작성
    override suspend fun createPost(challengerId: Long, request: CreatePost): ApiState<ContentItem> {
        val request = CreatePostRequest(request.title, request.content, request.category)
        return communityRemoteDataSource.createPost(challengerId, request).map {
            it.toContentItemDomain() }
    }

    //번개게시글 작성
    override suspend fun createLightningPost(challengerId: Long, request: CreateLightningPost): ApiState<ContentItem> {
        val request = CreatePostLightningRequest(request.title, request.content,
            request.meetAt, request.location, request.maxParticipants, request.openChatUrl)
        return communityRemoteDataSource.createLightningPost(challengerId,request).map {
            it.toContentItemDomain() }
    }

    //게시글 삭제하기
    override suspend fun deletePost(postId: Long): ApiState<Unit> {
        return communityRemoteDataSource.deletePost(postId)
    }

    //게시글 좋아요 토글하기
    override suspend fun togglePostLike(postId: Long): ApiState<PostLike> {
        return communityRemoteDataSource.togglePostLike(postId).map {
            it.toPostLikeDomain() }
    }

    //게시글 스크랩 토글하기
    override suspend fun togglePostScrap(postId: Long): ApiState<PostScrap> {
        return communityRemoteDataSource.togglePostScrap(postId).map {
            it.toPostScrapDomain() }
    }

    //게시글 댓글 삭제하기
    override suspend fun deleteComment(postId: Long, commentId: Long, challengerId: Long
    ): ApiState<Unit> {
        return communityRemoteDataSource.deleteComment(postId, commentId, challengerId)
    }

    //게시글 수정하기
    override suspend fun updatePost(postId: Long, request: CreatePost): ApiState<ContentItem> {
        val request = CreatePostRequest(request.title, request.content, request.category)
        return communityRemoteDataSource.updatePost(postId, request).map {
            it.toContentItemDomain() }
    }

    //내가 작성한 글 가져오기
    override suspend fun getMyPosts(page: Int, size: Int): ApiState<PostPageModel> {
        return communityRemoteDataSource.getMyPosts(page, size).map {
            it.toPostPageModelDomain() }
    }

    //내가 댓글단글 가져오기
    override suspend fun getMyCommentedPosts(page: Int, size: Int): ApiState<PostPageModel> {
        return communityRemoteDataSource.getMyCommentedPosts(page, size).map {
            it.toPostPageModelDomain() }
    }

    //내가 스크랩한 글 가져오기
    override suspend fun getMyScrappedPosts(page: Int, size: Int): ApiState<PostPageModel> {
        return communityRemoteDataSource.getMyScrappedPosts(page, size).map {
            it.toPostPageModelDomain() }
    }

}