package com.umc.domain.repository.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.CreateLightningPost
import com.umc.domain.model.community.CreatePost
import com.umc.domain.model.community.PostLike
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.model.community.PostScrap

interface CommunityRepository {

    //게시글 목록 자겨오기
    suspend fun getPosts(category: String?, page: Int, size: Int): ApiState<PostPageModel>

    // 게시글 상세 조회
    suspend fun getPostDetail(postId: Long): ApiState<ContentItem>

    // 댓글 목록 조회
    suspend fun getComments(postId: Long): ApiState<List<CommentItem>>

    // 댓글 작성
    suspend fun createComment(postId: Long, challengerId: Long, content: String, parentId: Long): ApiState<CommentItem>

    // 게시글 검색
    suspend fun searchPosts(keyword: String, page: Int, size: Int) : ApiState<PostPageModel>

    // 일반 게시글 작성
    suspend fun createPost(challengerId: Long, request: CreatePost): ApiState<ContentItem>

    // 번개 게시글 작성
    suspend fun createLightningPost(challengerId: Long, request: CreateLightningPost): ApiState<ContentItem>

    // 게시글 삭제하기
    suspend fun deletePost(postId: Long): ApiState<Unit>

    // 게시글 좋아요 토글하기
    suspend fun togglePostLike(postId: Long): ApiState<PostLike>

    // 게시글 스크랩 토글하기
    suspend fun togglePostScrap(postId: Long): ApiState<PostScrap>


    // 게시글 댓글 삭제하기
    suspend fun deleteComment(postId: Long, commentId: Long, challengerId: Long): ApiState<Unit>

    // 게시글 수정하기
    suspend fun updatePost(postId: Long, request: CreatePost): ApiState<ContentItem>

    //내 게시글 가져오기
    suspend fun getMyPosts(page: Int, size: Int): ApiState<PostPageModel>

    //내 댓글단 글 갖오기
    suspend fun getMyCommentedPosts(page: Int, size: Int): ApiState<PostPageModel>

    //내가 스크랩 한 글 갖고오기
    suspend fun getMyScrappedPosts(page: Int, size: Int): ApiState<PostPageModel>

}

