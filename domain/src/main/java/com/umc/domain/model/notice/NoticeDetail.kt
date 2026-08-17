package com.umc.domain.model.notice

data class NoticeDetail(
    val id: Long = -1L,
    val title: String = "",
    val content: String = "",
    // 작성자 판별(내 챌린저 ID와 비교)은 challengerId, 프로필 조회는 memberId를 쓴다
    val authorChallengerId: Long = -1L,
    val authorMemberId: Long = -1L,
    val mustRead: Boolean = false,
    val vote: NoticeVote? = null,
    val images: List<NoticeImage> = emptyList(),
    val links: List<NoticeLink> = emptyList(),
    val targetInfo: NoticeTarget = NoticeTarget(),
    val viewCount: Int = 0,
    val createdAt: String = ""
)

data class NoticeVote(
    val voteId: Long = -1L,
    val title: String = "",
    val isAnonymous: Boolean = false,
    val allowMultipleChoice: Boolean = false,
    val status: String = "",
    val startsAt: String = "",
    val endsAtExclusive: String = "",
    val totalParticipants: Int = 0,
    val options: List<NoticeVoteOption> = emptyList(),
    val mySelectedOptionIds: List<Long> = emptyList()
)

data class NoticeVoteParticipant(
    val memberId: Long = -1L,
    val nickname: String = "",
    val name: String = "",
    val profileImageUrl: String = ""
)

data class NoticeVoteOption(
    val optionId: Long = -1L,
    val content: String = "",
    val voteCount: Int = 0,
    val voteRate: Double = 0.0,
    val selectedMemberIds: List<Long> = emptyList()
)

data class NoticeImage(
    val id: Long = -1L,
    val url: String = "",
    val displayOrder: Int = 0
)

data class NoticeLink(
    val id: Long = -1L,
    val url: String = "",
    val displayOrder: Int = 0
)

data class NoticeTarget(
    val targetGisuId: Int = 0,
    val targetChapterId: Int? = null,
    val targetChapterName: String? = null,
    val targetSchoolId: Int? = null,
    val targetParts: List<String> = emptyList(),
    val targetNoticeTab: String = ""
)