package com.umc.domain.model.community

data class CommentItem (
    val commentId: Long,
    val postId: Long,
    val challengerId: Long,
    val challengerName: String?,
    val challengerNickName: String?,
    val challengerProfileImage: String,
    val content: String,
    val createdAt: String,   // "2026.02.07 15:16" 형태로 합쳐진 결과물
    val isMine: Boolean = false // UI에서 내 댓글 표시용 (필요 시 활용)
)