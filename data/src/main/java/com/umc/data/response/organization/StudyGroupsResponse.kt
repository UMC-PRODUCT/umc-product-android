package com.umc.data.response.organization

import com.umc.data.response.serializer.FlexibleIntSerializer
import com.umc.data.response.serializer.FlexibleLongNullableSerializer
import com.umc.data.response.serializer.FlexibleLongSerializer
import kotlinx.serialization.Serializable

@Serializable
data class StudyGroupListResponse(
    val content: List<StudyGroupResponse> = emptyList(),

    @Serializable(with = FlexibleLongNullableSerializer::class)
    val nextCursor: Long? = null,

    val hasNext: Boolean = false,
)


@Serializable
data class MyStudyGroupListResponse(
    val studyGroups: List<StudyGroupNameResponse>
)

@Serializable
data class StudyGroupNameResponse(
    val groupId: Int = 0,
    val name: String = "",
)

@Serializable
data class StudyGroupResponse(
    @Serializable(with = FlexibleLongSerializer::class)
    val groupId: Long = 0L,

    val name: String = "",

    @Serializable(with = FlexibleIntSerializer::class)
    val memberCount: Int = 0,

    val leader: GroupMemberResponse? = null,

    val members: List<GroupMemberResponse> = emptyList()
)

@Serializable
data class GroupMemberResponse(
    @Serializable(with = FlexibleLongSerializer::class)
    val challengerId: Long = 0L,

    @Serializable(with = FlexibleLongSerializer::class)
    val memberId: Long = 0L,

    val name: String = "",
    val profileImageUrl: String? = null
)