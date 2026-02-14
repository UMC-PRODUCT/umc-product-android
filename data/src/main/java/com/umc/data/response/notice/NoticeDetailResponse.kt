package com.umc.data.response.notice

import kotlinx.serialization.Serializable

@Serializable
data class NoticeDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorChallengerId: Long,
    val vote: NoticeVoteResponse?,
    val images: List<NoticeImageResponse>,
    val links: List<NoticeLinkResponse>,
    val targetInfo: NoticeTargetResponse,
    val viewCount: Int,
    val createdAt: String
)

@Serializable
data class NoticeVoteResponse(
    val voteId: Long,
    val title: String,
    val isAnonymous: Boolean,
    val allowMultipleChoice: Boolean,
    val status: String,
    val startsAt: String,
    val endsAtExclusive: String,
    val startDateKst: String,
    val endDateKst: String,
    val totalParticipants: Int,
    val options: List<NoticeVoteOptionResponse>,
    val mySelectedOptionIds: List<Long>
)

@Serializable
data class NoticeVoteOptionResponse(
    val optionId: Long,
    val content: String,
    val voteCount: Int,
    val voteRate: Double
)

@Serializable
data class NoticeImageResponse(
    val id: Long,
    val url: String,
    val displayOrder: Int
)

@Serializable
data class NoticeLinkResponse(
    val id: Long,
    val url: String,
    val displayOrder: Int
)

@Serializable
data class NoticeTargetResponse(
    val targetGisuId: Int,
    val targetChapterId: Int?,
    val targetSchoolId: Int?,
    val targetParts: List<String>
)