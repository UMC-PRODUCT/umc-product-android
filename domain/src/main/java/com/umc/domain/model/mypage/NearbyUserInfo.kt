package com.umc.domain.model.mypage

import com.google.gson.Gson

/**이는 명함 교환하기 시 상대방에게 보여줄 내 정보를 위한 Data Class 입니다. (유저 카드랑은 별개)**/
data class NearbyUserInfo(
    val name: String,
    val info: String,
    val profileImageLink: String = "",
) {
    /*TODO nearbyConnection의 경우, advertise name이 120바이트 이내여야 한다.
    *
    * 즉, 프로필 이미지 때문에 짤리면 이름이랑 정보만 가지고 다시 재생성(이미지는null)
    * **/
    fun toJson(): String {
        val gson = Gson()
        val fullJson = gson.toJson(this)

        // UTF-8 기준 바이트 크기 체크 (안전 범위 120바이트 이하)
        if (fullJson.toByteArray(Charsets.UTF_8).size <= 120) {
            return fullJson
        }

        // 바이트 초과 시 프로필 이미지를 제외(null)하고 이름과 정보만 포함하여 재생성
        val fallbackUser = this.copy(profileImageLink = "")
        return gson.toJson(fallbackUser)
    }

    companion object {
        fun fromJson(json: String): NearbyUserInfo? {
            return try {
                Gson().fromJson(json, NearbyUserInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}