package com.umc.domain.model.notice

data class NoticeReadStatus(
    val content: List<ChallengerReadInfo>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

data class ChallengerReadInfo(
    val challengerId: Long,
    val name: String,
    val profileImageUrl: String,
    val part: String,
    val schoolId: Long,
    val schoolName: String,
    val chapterId: Long,
    val chapterName: String
)