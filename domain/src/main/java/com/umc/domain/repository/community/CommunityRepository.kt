package com.umc.domain.repository.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.PostPageModel

interface CommunityRepository {

    //게시글 목록 자겨오기
    suspend fun getPosts(
        ing: Boolean,
        sort: String,
        page: Int,
        size: Int
    ): ApiState<PostPageModel>

    // 게시글 상세 조회
    suspend fun getPostDetail(postId: Long): ApiState<ContentItem>

    // 댓글 목록 조회
    suspend fun getComments(postId: Long): ApiState<List<CommentItem>>

    // 댓글 작성
    suspend fun createComment(postId: Long, challengerId: Long, content: String, parentId: Long): ApiState<CommentItem>



}