package com.umc.data.response.notice

import com.umc.data.response.notice.ChallengerReadInfoResponse.Companion.toModel
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeReadStatus
import kotlinx.serialization.Serializable

@Serializable
data class NoticeReadStatusResponse(
    val content: List<ChallengerReadInfoResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean
) {
    companion object {
        fun NoticeReadStatusResponse.toModel(): NoticeReadStatus = NoticeReadStatus(
            content = content.map { it.toModel() },
            nextCursor = nextCursor,
            hasNext = hasNext
        )
    }
}

@Serializable
data class ChallengerReadInfoResponse(
    val challengerId: Long,
    val name: String,
    val profileImageUrl: String?,
    val part: String,
    val schoolId: Long,
    val schoolName: String,
    val chapterId: Long,
    val chapterName: String
) {
    companion object {
        fun ChallengerReadInfoResponse.toModel(): ChallengerReadInfo = ChallengerReadInfo(
            challengerId = challengerId,
            name = name,
            profileImageUrl = profileImageUrl ?: "",
            part = part,
            schoolId = schoolId,
            schoolName = schoolName,
            chapterId = chapterId,
            chapterName = chapterName
        )
    }
}