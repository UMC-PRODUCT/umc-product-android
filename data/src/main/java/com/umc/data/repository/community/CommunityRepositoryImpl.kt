package com.umc.data.repository.community

import com.umc.data.dataSource.remote.community.CommunityRemoteDataSource
import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.request.community.CreatePostLightningRequest
import com.umc.data.request.community.CreatePostRequest
import com.umc.data.response.community.CommunityGetPostResponse.Companion.toPostPageModelDomain
import com.umc.data.response.community.CommunitySearchPostResponse.Companion.toPostPageModelDomain
import com.umc.data.response.community.PostCommentResponse.Companion.toCommentItemDomain
import com.umc.data.response.community.PostDetailResponse.Companion.toContentItemDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.model.community.CreatePost
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityRemoteDataSource: CommunityRemoteDataSource
) : CommunityRepository {

    //게시글 작성
    override suspend fun getPosts(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
    ): ApiState<PostPageModel> {
        return communityRemoteDataSource.getPosts(ing, sort, page, size).map {
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
    override suspend fun createPost(request: CreatePost): ApiState<ContentItem> {
        val request = CreatePostRequest(request.title, request.content, request.category, request.region, request.anonymous)
        return communityRemoteDataSource.createPost(request).map {
            it.toContentItemDomain() }
    }

    //번개게시글 작성
    override suspend fun createLightningPost(request: CreateLightningPost): ApiState<ContentItem> {
        val request = CreatePostLightningRequest(request.title, request.content, request.region, request.anonymous,
            request.meetAt, request.location, request.maxParticipants)
        return communityRemoteDataSource.createLightningPost(request).map {
            it.toContentItemDomain() }
    }




}