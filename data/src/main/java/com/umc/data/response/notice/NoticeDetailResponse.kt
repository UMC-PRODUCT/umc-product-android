package com.umc.data.response.notice

import com.umc.data.response.notice.NoticeImageResponse.Companion.toModel
import com.umc.data.response.notice.NoticeLinkResponse.Companion.toModel
import com.umc.data.response.notice.NoticeTargetResponse.Companion.toModel
import com.umc.data.response.notice.NoticeVoteOptionResponse.Companion.toModel
import com.umc.data.response.notice.NoticeVoteResponse.Companion.toModel
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeImage
import com.umc.domain.model.notice.NoticeLink
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import kotlinx.serialization.Serializable

@Serializable
data class NoticeDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorMemberId: Long,
    val vote: NoticeVoteResponse?,
    val images: List<NoticeImageResponse>,
    val links: List<NoticeLinkResponse>,
    val targetInfo: NoticeTargetResponse,
    val viewCount: Int,
    val createdAt: String
) {
    companion object {
        fun NoticeDetailResponse.toModel(): NoticeDetail = NoticeDetail(
            id = id,
            title = title,
            content = content,
            authorChallengerId = authorMemberId,
            vote = vote?.toModel(),
            images = images.map { it.toModel() },
            links = links.map { it.toModel() },
            targetInfo = targetInfo.toModel(),
            viewCount = viewCount,
            createdAt = createdAt
        )
    }
}

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
) {
    companion object {
        fun NoticeVoteResponse.toModel(): NoticeVote = NoticeVote(
            voteId = voteId,
            title = title,
            isAnonymous = isAnonymous,
            allowMultipleChoice = allowMultipleChoice,
            status = status,
            startsAt = startsAt,
            endsAtExclusive = endsAtExclusive,
            startDateKst = startDateKst,
            endDateKst = endDateKst,
            totalParticipants = totalParticipants,
            options = options.map { it.toModel() },
            mySelectedOptionIds = mySelectedOptionIds
        )
    }
}

@Serializable
data class NoticeVoteOptionResponse(
    val optionId: Long,
    val content: String,
    val voteCount: Int,
    val voteRate: Double,
    val selectedMemberIds: List<Long>? = null
) {
    companion object {
        fun NoticeVoteOptionResponse.toModel(): NoticeVoteOption = NoticeVoteOption(
            optionId = optionId,
            content = content,
            voteCount = voteCount,
            voteRate = voteRate,
            selectedMemberIds = selectedMemberIds ?: emptyList()
        )
    }
}

@Serializable
data class NoticeImageResponse(
    val id: Long,
    val url: String,
    val displayOrder: Int
) {
    companion object {
        fun NoticeImageResponse.toModel(): NoticeImage = NoticeImage(
            id = id,
            url = url,
            displayOrder = displayOrder
        )
    }
}

@Serializable
data class NoticeLinkResponse(
    val id: Long,
    val url: String,
    val displayOrder: Int
) {
    companion object {
        fun NoticeLinkResponse.toModel(): NoticeLink = NoticeLink(
            id = id,
            url = url,
            displayOrder = displayOrder
        )
    }
}

@Serializable
data class NoticeTargetResponse(
    val targetGisuId: String?,
    val targetChapterId: String?,
    val targetSchoolId: String?,
    val targetParts: List<String>?
) {
    companion object {
        fun NoticeTargetResponse.toModel(): NoticeTarget = NoticeTarget(
            targetGisuId = targetGisuId?.toIntOrNull() ?: 0,
            targetChapterId = targetChapterId?.toIntOrNull(),
            targetSchoolId = targetSchoolId?.toIntOrNull(),
            targetParts = targetParts ?: emptyList()
        )
    }
}