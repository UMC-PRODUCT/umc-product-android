package com.umc.domain.model.community.thread

data class CommunityThreadList(
    val pinned: List<CommunityThreadSummary> = emptyList(),
    val threads: List<CommunityThreadSummary> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)

data class CommunityThreadSummary(
    val threadId: String = "",
    val title: String = "",
    val description: String? = null,
    val category: CommunityThreadCategory = CommunityThreadCategory.FREE,
    val icon: String = "",
    val memberCount: String = "0",
    val unreadCount: String = "0",
    val maxMembers: String = "100",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val myRole: CommunityThreadRole = CommunityThreadRole.MEMBER,
    val lastMessage: CommunityThreadLastMessage? = null,
    val createdBy: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class CommunityThreadDetail(
    val threadId: String = "",
    val title: String = "",
    val description: String? = null,
    val category: CommunityThreadCategory = CommunityThreadCategory.FREE,
    val icon: String = "",
    val memberCount: String = "0",
    val unreadCount: String = "0",
    val maxMembers: String = "100",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val myRole: CommunityThreadRole = CommunityThreadRole.MEMBER,
    val lastMessage: CommunityThreadLastMessage? = null,
    val createdBy: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val shareUrl: String? = null,
    val deletedAt: String? = null,
)

data class CommunityThreadLastMessage(
    val preview: String = "",
    val senderName: String = "",
    val createdAt: String = "",
)

data class CommunityThreadMessagePage(
    val messages: List<CommunityThreadMessage> = emptyList(),
    val hasMore: Boolean = false,
    val nextBefore: String? = null,
)

data class CommunityThreadMessage(
    val messageId: String = "",
    val threadId: String = "",
    val senderId: String? = null,
    val senderName: String? = null,
    val content: String? = null,
    val type: CommunityMessageType = CommunityMessageType.TEXT,
    val status: String = "SENT",
    val fileMetadataIds: List<String> = emptyList(),
    val mentions: List<CommunityMention> = emptyList(),
    val replyTo: CommunityReplyTo? = null,
    val reactions: List<CommunityReaction> = emptyList(),
    val clientMessageId: String? = null,
    val createdAt: String = "",
    val editedAt: String? = null,
    val deletedAt: String? = null,
)

data class CommunityMention(val memberId: String = "", val name: String = "")
data class CommunityReplyTo(
    val messageId: String = "",
    val senderName: String = "",
    val snippet: String = "",
)
data class CommunityReaction(
    val emoji: String = "",
    val count: String = "0",
    val reactedByMe: Boolean = false,
)

data class CommunityThreadMemberPage(
    val items: List<CommunityThreadMember> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)

data class CommunityThreadMember(
    val memberId: String = "",
    val name: String = "",
    val profileImageUrl: String = "",
    val part: String? = null,
    val generation: String? = null,
    val role: CommunityThreadRole = CommunityThreadRole.MEMBER,
    val joinedAt: String = "",
    val state: CommunityThreadMemberState = CommunityThreadMemberState.ACTIVE,
)

data class CommunityThreadInvitablePage(
    val items: List<CommunityThreadInvitable> = emptyList(),
    val nextOffset: String? = null,
    val total: String = "0",
)

data class CommunityThreadInvitable(
    val memberId: String = "",
    val challengerId: String? = null,
    val name: String = "",
    val part: String? = null,
    val generation: String? = null,
)

data class CommunityThreadInvitation(
    val invitedMembers: List<CommunityThreadMember> = emptyList(),
    val memberCount: String = "0",
)

data class CommunityThreadMemberMutation(
    val threadId: String = "",
    val memberId: String = "",
    val role: CommunityThreadRole = CommunityThreadRole.MEMBER,
    val state: CommunityThreadMemberState = CommunityThreadMemberState.ACTIVE,
    val memberCount: String = "0",
)

data class CommunityMessageReportReceipt(
    val reportId: String = "",
    val messageId: String = "",
    val reason: CommunityMessageReportReason = CommunityMessageReportReason.ETC,
    val createdAt: String = "",
)

data class CreateCommunityThread(
    val title: String,
    val description: String? = null,
    val category: CommunityThreadCategory,
    val icon: String,
    val memberIds: List<Long> = emptyList(),
)

data class UpdateCommunityThread(
    val title: String? = null,
    val description: String? = null,
    val category: CommunityThreadCategory? = null,
    val icon: String? = null,
)

enum class CommunityThreadCategory { STUDY, QNA, PROJECT, FREE }
enum class CommunityThreadRole { OWNER, ADMIN, MEMBER }
enum class CommunityThreadMemberState { ACTIVE, LEFT, KICKED }
enum class CommunityMessageType { TEXT, IMAGE, SYSTEM }
enum class CommunityMessageReportReason { SPAM, ABUSE, INAPPROPRIATE, PRIVACY, ETC }
