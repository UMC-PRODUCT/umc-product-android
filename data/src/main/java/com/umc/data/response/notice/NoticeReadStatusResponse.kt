package com.umc.data.response.notice

import com.umc.data.response.notice.ChallengerReadInfoResponse.Companion.toModel
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeReadStatus
import kotlinx.serialization.Serializable

@Serializable
data class NoticeReadStatusResponse(
    val content: List<ChallengerReadInfoResponse>?,
    val nextCursor: Long?,
    val hasNext: Boolean
) {
    companion object {
        fun NoticeReadStatusResponse.toModel(): NoticeReadStatus = NoticeReadStatus(
            content = content.orEmpty().map { it.toModel() },
            nextCursor = nextCursor,
            hasNext = hasNext
        )
    }
}

@Serializable
data class ChallengerReadInfoResponse(
    // Gson은 응답에 없는 필드를 null로 남기므로(파트가 없는 챌린저는 part 생략) 모두 nullable로 받고 toModel에서 채운다
    val challengerId: Long,
    val name: String?,
    val profileImageUrl: String?,
    val part: String?,
    val schoolId: Long?,
    val schoolName: String?,
    val chapterId: Long?,
    val chapterName: String?
) {
    companion object {
        fun ChallengerReadInfoResponse.toModel(): ChallengerReadInfo = ChallengerReadInfo(
            challengerId = challengerId,
            name = name.orEmpty(),
            profileImageUrl = profileImageUrl.orEmpty(),
            part = part.orEmpty(),
            schoolId = schoolId ?: 0L,
            schoolName = schoolName.orEmpty(),
            chapterId = chapterId ?: 0L,
            chapterName = chapterName.orEmpty()
        )
    }
}