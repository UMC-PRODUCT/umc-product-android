package com.umc.presentation.ui.community.detail

import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem

sealed class PostDetailItem {
    // 1. 게시글 본문
    data class Header(val post: ContentItem) : PostDetailItem()

    // 2. 댓글 개수 표시 바
    data class CommentHeader(val count: Int) : PostDetailItem()

    // 3. 실제 댓글 리스트
    data class Comment(val comment: CommentItem) : PostDetailItem()

    // 4. 댓글이 없을 때 보여줄 빈 화면
    object EmptyComment : PostDetailItem()
}