package com.umc.data.response

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel

data class ChallengerResponse(
    @SerializedName("challengerId") val challengerId: Long? = null,
    @SerializedName("memberId") val memberId: Long? = null,
    @SerializedName("gisu") val gisu: Int? = null,
    @SerializedName("part") val part: String? = null,
    @SerializedName("challengerPoints") val challengerPoints: List<PointResponse>? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("schoolId") val schoolId: Int? = null,
    @SerializedName("schoolName") val schoolName: String? = null,
    @SerializedName("profileImageLink") val profileImageLink: String? = null,
    @SerializedName("status") val status: String? = null
) {
    companion object {
        fun ChallengerResponse.toModel(): ChallengerInfoDialogModel {
            val defaultModel = ChallengerInfoDialogModel()

            return ChallengerInfoDialogModel(
                name = name ?: defaultModel.name,
                university = schoolName ?: defaultModel.university,
                part = part ?: defaultModel.part,
                generation = gisu ?: defaultModel.generation,
                profileImageUrl = profileImageLink ?: defaultModel.profileImageUrl
            )
        }

        fun ChallengerResponse.toManageModel(): ChallengerManageDialogModel {
            val defaultModel = ChallengerManageDialogModel()

            return ChallengerManageDialogModel(
                name = name ?: defaultModel.name,
                university = schoolName ?: defaultModel.university,
                part = part ?: defaultModel.part,
                profileImageUrl = profileImageLink ?: defaultModel.profileImageUrl
            )
        }
    }
}

data class PointResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("pointType") val pointType: String? = null,
    @SerializedName("point") val point: Double? = null,
    @SerializedName("description") val description: String? = null
)