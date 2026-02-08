package com.umc.domain.repository.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.model.community.CreatePost
import com.umc.domain.model.community.PostPageModel

interface CommunityRepository {

    //게시글 목록 자겨오기
    suspend fun getPosts(ing: Boolean, sort: String, page: Int, size: Int): ApiState<PostPageModel>

    // 게시글 상세 조회
    suspend fun getPostDetail(postId: Long): ApiState<ContentItem>

    // 댓글 목록 조회
    suspend fun getComments(postId: Long): ApiState<List<CommentItem>>

    // 댓글 작성
    suspend fun createComment(postId: Long, challengerId: Long, content: String, parentId: Long): ApiState<CommentItem>

    // 게시글 검색
    suspend fun searchPosts(keyword: String, page: Int, size: Int) : ApiState<PostPageModel>

    // 일반 게시글 작성
    suspend fun createPost(request: CreatePost): ApiState<ContentItem>

    // 번개 게시글 작성
    suspend fun createLightningPost(request: CreateLightningPost): ApiState<ContentItem>



}