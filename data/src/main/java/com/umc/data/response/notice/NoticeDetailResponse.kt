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

/**
 * 공지 상세 응답. 서버 스펙(GetNoticeDetailResponse)상 필수 필드가 없어
 * 모든 필드를 nullable로 두고 매핑 단계에서 기본값을 채운다.
 * (Gson은 응답에 없는 필드를 null로 남기므로 non-null로 선언하면 매핑에서 NPE가 난다)
 */
@Serializable
data class NoticeDetailResponse(
    val id: Long?,
    val title: String?,
    val content: String?,
    val authorChallengerId: Long?,
    val authorMemberId: Long?,
    val mustRead: Boolean?,
    val vote: NoticeVoteResponse?,
    val images: List<NoticeImageResponse>?,
    val links: List<NoticeLinkResponse>?,
    val targetInfo: NoticeTargetResponse?,
    val viewCount: Int?,
    val createdAt: String?
) {
    companion object {
        fun NoticeDetailResponse.toModel(): NoticeDetail = NoticeDetail(
            id = id ?: -1L,
            title = title.orEmpty(),
            content = content.orEmpty(),
            authorChallengerId = authorChallengerId ?: -1L,
            authorMemberId = authorMemberId ?: -1L,
            mustRead = mustRead ?: false,
            vote = vote?.toModel(),
            images = images?.map { it.toModel() }.orEmpty(),
            links = links?.map { it.toModel() }.orEmpty(),
            targetInfo = targetInfo?.toModel() ?: NoticeTarget(),
            viewCount = viewCount ?: 0,
            createdAt = createdAt.orEmpty()
        )
    }
}

@Serializable
data class NoticeVoteResponse(
    val voteId: Long?,
    val title: String?,
    val isAnonymous: Boolean?,
    val allowMultipleChoice: Boolean?,
    val status: String?,
    val startsAt: String?,
    val endsAtExclusive: String?,
    val totalParticipants: Int?,
    val options: List<NoticeVoteOptionResponse>?,
    val mySelectedOptionIds: List<Long>?
) {
    companion object {
        fun NoticeVoteResponse.toModel(): NoticeVote = NoticeVote(
            voteId = voteId ?: -1L,
            title = title.orEmpty(),
            isAnonymous = isAnonymous ?: false,
            allowMultipleChoice = allowMultipleChoice ?: false,
            status = status.orEmpty(),
            startsAt = startsAt.orEmpty(),
            endsAtExclusive = endsAtExclusive.orEmpty(),
            totalParticipants = totalParticipants ?: 0,
            options = options?.map { it.toModel() }.orEmpty(),
            mySelectedOptionIds = mySelectedOptionIds.orEmpty()
        )
    }
}

@Serializable
data class NoticeVoteOptionResponse(
    val optionId: Long?,
    val content: String?,
    val voteCount: Int?,
    val voteRate: Double?,
    val selectedMemberIds: List<Long>? = null
) {
    companion object {
        fun NoticeVoteOptionResponse.toModel(): NoticeVoteOption = NoticeVoteOption(
            optionId = optionId ?: -1L,
            content = content.orEmpty(),
            voteCount = voteCount ?: 0,
            voteRate = voteRate ?: 0.0,
            selectedMemberIds = selectedMemberIds.orEmpty()
        )
    }
}

@Serializable
data class NoticeImageResponse(
    val id: Long?,
    val url: String?,
    val displayOrder: Int?
) {
    companion object {
        fun NoticeImageResponse.toModel(): NoticeImage = NoticeImage(
            id = id ?: -1L,
            url = url.orEmpty(),
            displayOrder = displayOrder ?: 0
        )
    }
}

@Serializable
data class NoticeLinkResponse(
    val id: Long?,
    val url: String?,
    val displayOrder: Int?
) {
    companion object {
        fun NoticeLinkResponse.toModel(): NoticeLink = NoticeLink(
            id = id ?: -1L,
            url = url.orEmpty(),
            displayOrder = displayOrder ?: 0
        )
    }
}

@Serializable
data class NoticeTargetResponse(
    val targetGisuId: Int?,
    val targetChapterId: Int?,
    val targetSchoolId: Int?,
    val targetParts: List<String>?,
    val targetNoticeTab: String?
) {
    companion object {
        fun NoticeTargetResponse.toModel(): NoticeTarget = NoticeTarget(
            targetGisuId = targetGisuId ?: 0,
            targetChapterId = targetChapterId,
            targetSchoolId = targetSchoolId,
            targetParts = targetParts.orEmpty(),
            targetNoticeTab = targetNoticeTab.orEmpty()
        )
    }
}
