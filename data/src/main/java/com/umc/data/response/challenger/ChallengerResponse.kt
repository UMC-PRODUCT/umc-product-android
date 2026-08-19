package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.UserPart

data class ChallengerResponse(
    @SerializedName("challengerId") val challengerId: Long? = null,
    @SerializedName("memberId") val memberId: Long? = null,
    @SerializedName("gisu") val gisu: Int? = null,
    @SerializedName("part") val part: String? = null,
    @SerializedName("challengerPoints") val challengerPoints: List<ChallengerPointResponse>? = null,
    @SerializedName("points") val points: List<ChallengerPointResponse>? = null,
    @SerializedName("totalPoints") val totalPoints: Double? = null,
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
                part = UserPart.from(part),
                generation = gisu ?: defaultModel.generation,
                profileImageUrl = profileImageLink ?: defaultModel.profileImageUrl,
                totalPoints = totalPoints ?: defaultModel.totalPoints
            )
        }
        fun ChallengerResponse.toManageModel(): ChallengerManageDialogModel {
            val defaultModel = ChallengerManageDialogModel()

            val rawPoints = when {
                !points.isNullOrEmpty() -> points
                !challengerPoints.isNullOrEmpty() -> challengerPoints
                else -> emptyList()
            }

            val pointList = rawPoints
                .filter { point ->
                    point.pointType != null
                }
                .map { point ->
                    val parsedPointType = try {
                        PointType.valueOf(point.pointType!!)
                    } catch (_: Exception) {
                        PointType.CUSTOM
                    }

                    ChallengerPoint(
                        id = point.id ?: 0L,
                        date = point.createdAt.toDateOnly(),
                        title = point.description?.takeIf { it.isNotBlank() } ?: "사유 없음",
                        pointType = parsedPointType,
                        value = point.point ?: 0.0
                    )
                }

            return ChallengerManageDialogModel(
                challengerId = challengerId ?: defaultModel.challengerId,
                name = name ?: defaultModel.name,
                nickname = nickname ?: defaultModel.nickname,
                university = schoolName ?: defaultModel.university,
                part = UserPart.from(part),
                gisu = gisu ?: defaultModel.gisu,
                profileImageUrl = profileImageLink ?: defaultModel.profileImageUrl,
                totalScore = totalPoints ?: defaultModel.totalScore,
                rewardScore = pointList.filter { it.value > 0 }.sumOf { it.value }.toInt(),
                penaltyScore = pointList.filter { it.value < 0 }.sumOf { -it.value }.toInt(),
                history = pointList
            )
        }
    }
}

private fun String?.toDateOnly(): String = orEmpty()
    .substringBefore('T')
    .substringBefore(' ')
