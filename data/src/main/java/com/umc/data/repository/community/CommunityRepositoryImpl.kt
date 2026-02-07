package com.umc.data.repository.community

import com.umc.data.dataSource.remote.community.CommunityRemoteDataSource
import com.umc.data.request.community.CreateCommentRequest
import com.umc.data.response.community.CommunityGetPostResponse.Companion.toCommunityDomain
import com.umc.data.response.community.PostCommentResponse.Companion.toCommunityDomain
import com.umc.data.response.community.PostDetailResponse.Companion.toCommunityDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
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
            it.toCommunityDomain() }
    }

    //게시글 상세
    override suspend fun getPostDetail(postId: Long): ApiState<ContentItem> {
        return communityRemoteDataSource.getPostDetail(postId).map {
            it.toCommunityDomain() }
    }

    //게시글 댓글 조회
    override suspend fun getComments(postId: Long): ApiState<List<CommentItem>> {
        return communityRemoteDataSource.getComments(postId).map {
            it.map { it.toCommunityDomain() } }
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
            it.toCommunityDomain() }

    }

}