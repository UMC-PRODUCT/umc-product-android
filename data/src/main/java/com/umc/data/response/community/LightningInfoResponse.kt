package com.umc.data.response.community

import com.google.gson.annotations.SerializedName


/**번개 게시글 일때, 정보
 * 인자 추가 가능
 *
 * **/
data class LightningInfoResponse(
    @SerializedName("meetAt") val meetAt: String,         // 모임 일시
    @SerializedName("location") val location: String,      // 모임 장소
    @SerializedName("maxParticipants") val maxParticipants: Int // 모임 최대 참여 가능 인원
)

