package com.umc.domain.model.mypage
import com.google.gson.Gson
import java.awt.Color

data class UserCard(
    val id: String,
    val name: String,
    val nickname: String,
    val university: String = "",
    val part: UserCardPartType = UserCardPartType.ADMIN,
    val generation: Int = 10,
    val profileImage: String? = null,

    /**차후 추가 예정**/

    val email: String? = null,
    val github: String? = null,
    val blog: String? = null,
    val memberNo: String? = null,
    val qrPayload: String? = null,

){
    /**class <-> Json**/
    fun toJson(): String = Gson().toJson(this)
    companion object {
        fun fromJson(json: String): UserCard = Gson().fromJson(json, UserCard::class.java)
    }
}


/**명함에서 파트에 따른 색깔 변화 정의**/
enum class UserCardPartType(
    val label: String,
    val mainColorHex: Long // 대표 메인 색상 (100% 알파 기준)
) {
    ADMIN("Admin", 0xFF6155F5),
    PM("PM", 0xFFCB30E0),
    DESIGN("Design", 0xFFFF2D55),
    WEB("Web", 0xFFAC7F5E),
    ANDROID("Android", 0xFF00C0E8),
    SPRING("Spring", 0xFF34C759),
    NODEJS("Node.js", 0xFFFFCC00),
    IOS("iOS", 0xFFFF8D28)
}