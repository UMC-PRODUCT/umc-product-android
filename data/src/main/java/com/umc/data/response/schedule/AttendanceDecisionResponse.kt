package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName

data class AttendanceDecisionResponse(
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("excuseReason") val excuseReason: String?,
    @SerializedName("isPendingDecision") val isPendingDecision: Boolean?,
    @SerializedName("hasDecisionMakerMember") val hasDecisionMakerMember: Boolean?,
    @SerializedName("decisionMakerMemberInfo") val decisionMakerMemberInfo: DecisionMakerMemberInfo?,
    @SerializedName("decidedAt") val decidedAt: String?
) {
    data class DecisionMakerMemberInfo(
        @SerializedName("memberId") val memberId: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("nickname") val nickname: String?,
        @SerializedName("schoolId") val schoolId: String?,
        @SerializedName("schoolName") val schoolName: String?
    )
}
