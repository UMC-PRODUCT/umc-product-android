package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.community.CommentItem

data class PostCommentResponse(
    @SerializedName("commentId") val commentId: Long,
    @SerializedName("postId") val postId: Long,
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("challengerName") val challengerName: String?,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String?
) {
    companion object {
        fun PostCommentResponse.toCommentItemDomain(): CommentItem {
            val formatTime = if (this.createdAt.isNullOrBlank()) {
                ""
            } else {
                val (writeDay, writeClock) = this.createdAt.parseDateTime()
                "${writeDay} ${writeClock}"
            }

            return CommentItem(
                commentId = this.commentId,
                postId = this.postId,
                challengerId = this.challengerId,
                challengerName = this.challengerName,
                content = this.content,
                createdAt = formatTime
            )
        }

    }
}