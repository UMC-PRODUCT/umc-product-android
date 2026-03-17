package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.TrophyBody
import com.umc.domain.model.enums.UserPart

data class TrophyResponse(
    @SerializedName("trophyId") val trophyId: Long,
    @SerializedName("challengerId") val challengerId: Long,
    @SerializedName("week") val week: String,
    @SerializedName("challengerName") val challengerName: String,
    @SerializedName("challengerProfileImage") val challengerProfileImage: String?,
    @SerializedName("school") val school: String?,
    @SerializedName("part") val part: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("url") val url: String
) {
    companion object{
        fun TrophyResponse.toTrophyBody() : TrophyBody{
            val userPart = try {
                UserPart.valueOf(this.part)
            } catch (e: Exception) {
                UserPart.UNKNOWN
            }

            return TrophyBody(
                trophyId = this.trophyId,
                challengerId = this.challengerId,
                week = this.week.toIntOrNull() ?: 0,
                challengerName = this.challengerName,
                challengerProfileImage = this.challengerProfileImage ?: "",
                school = this.school ?: "",
                part = userPart,
                title = this.title,
                content = this.content,
                url = this.url
            )
        }

    }

}
