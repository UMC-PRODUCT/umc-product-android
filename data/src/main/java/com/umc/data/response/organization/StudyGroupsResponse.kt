package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class StudyGroupListResponse(
    val content: List<StudyGroupResponse> = emptyList(),
    val nextCursor: Int = 0,
    val hasNext: Boolean = false,
)

@Serializable
data class StudyGroupResponse(
    val groupId: Int = 0,
    val name: String = "",
    val memberCount: Int = 0,
    val leader: GroupMemberResponse = GroupMemberResponse(),
    val members: List<GroupMemberResponse> = emptyList()
)

@Serializable
data class GroupMemberResponse(
    val challengerId: Int = 0,
    val memberId: Int = 0,
    val name: String = "",
    val profileImageUrl: String? = ""
)
