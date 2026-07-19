package com.umc.domain.model.mypage
import com.google.gson.Gson

data class UserCard(
    val name: String,
    val nickname: String
    /**차후 추가 예정**/
){
    /**class <-> Json**/
    fun toJson(): String = Gson().toJson(this)
    companion object {
        fun fromJson(json: String): UserCard = Gson().fromJson(json, UserCard::class.java)
    }
}
