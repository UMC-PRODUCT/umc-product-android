package com.umc.domain.model.mypage

import com.google.gson.Gson

/**이는 명함 교환하기 시 상대방에게 보여줄 내 정보를 위한 Data Class 입니다. (유저 카드랑은 별개)**/
data class NearbyUserInfo(
    val name: String,
    val info: String,
    val profileImageLink: String = "",
) {
    fun toJson(): String = Gson().toJson(this)

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