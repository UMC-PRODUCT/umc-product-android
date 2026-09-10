package com.umc.data.response.organization

import com.google.gson.annotations.SerializedName

data class ManagedStudyGroupListResponse(
    @SerializedName("content")
    val content: List<ManagedStudyGroupResponse>?,

    @SerializedName("nextCursor")
    val nextCursor: Long?,

    @SerializedName("hasNext")
    val hasNext: Boolean?,
)

data class ManagedStudyGroupResponse(
    @SerializedName("studyGroupId")
    val studyGroupId: Long?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("gisuId")
    val gisuId: Long?,

    @SerializedName("studyPart")
    val studyPart: String?,

    // 학습 유형이 TRACK 인 기수는 studyPart 대신 이 값이 채워져 온다
    @SerializedName("track")
    val track: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("mentors")
    val mentors: List<ManagedStudyGroupMemberResponse>?,

    @SerializedName("members")
    val members: List<ManagedStudyGroupMemberResponse>?,
)

data class ManagedStudyGroupMemberResponse(
    @SerializedName("memberId")
    val memberId: Long?,

    @SerializedName("memberName")
    val memberName: String?,

    @SerializedName("schoolId")
    val schoolId: Long?,

    @SerializedName("schoolName")
    val schoolName: String?,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
)