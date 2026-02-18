package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.enums.PointType

data class ChallengerResponse(
    @SerializedName("challengerId") val challengerId: Long? = null,
    @SerializedName("memberId") val memberId: Long? = null,
    @SerializedName("gisu") val gisu: Int? = null,
    @SerializedName("part") val part: String? = null,
    @SerializedName("challengerPoints") val challengerPoints: List<ChallengerPointResponse>? = null,
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

            val pointList = challengerPoints?.filter { point ->
                point.pointType == "WARNING" || point.pointType == "OUT"
            }?.map { point ->
                ChallengerPoint(
                    id = point.id ?: 0L,
                    date = point.createdAt ?: "",
                    title = point.description ?: "사유 없음",
                    pointType = try {
                        PointType.valueOf(point.pointType ?: "WARNING")
                    } catch (_: Exception) {
                        PointType.WARNING
                    },
                    value = point.point ?: 0.0
                )
            } ?: emptyList()

            return ChallengerManageDialogModel(
                challengerId = challengerId ?: 0L,
                name = name ?: defaultModel.name,
                university = schoolName ?: defaultModel.university,
                part = part ?: defaultModel.part,
                profileImageUrl = profileImageLink ?: defaultModel.profileImageUrl,
                history = pointList,
                warningCount = pointList.count { it.pointType == PointType.WARNING },
                absenceCount = pointList.count { it.pointType == PointType.OUT }
            )
        }
    }
}