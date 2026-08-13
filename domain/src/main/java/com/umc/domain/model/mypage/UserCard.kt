package com.umc.domain.model.mypage
import com.google.gson.Gson
import java.awt.Color

data class UserCard(
    val name: String = "",
    val nickname: String = "",
    val university: String = "",
    val part: String = "",
    val generation: String = "0",


    /**차후 추가 예정**/
    val avatarURL: String? = null,
    val email: String? = null,
    val github: String? = null,
    val blog: String? = null,
    val memberNo: String? = null,
    val qrPayload: String? = null,

){
    /**class <-> Json**/
    fun toJson(): String = Gson().toJson(this)

    val cardId: String
        get() = "${name.trim()}_${nickname.trim()}"

    val partType: UserCardPartType
        get() = UserCardPartType.from(part)

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
    IOS("iOS", 0xFFFF8D28);

    companion object {
        // iOS/서버 문맥의 파트 문자열을 대응되는 Enum으로 매핑
        fun from(partStr: String): UserCardPartType {
            val upperPart = partStr.uppercase()
            return when {
                upperPart.contains("ANDROID") -> ANDROID
                upperPart.contains("IOS") -> IOS
                upperPart.contains("SPRING") -> SPRING
                upperPart.contains("NODE") -> NODEJS
                upperPart.contains("WEB") -> WEB
                upperPart.contains("DESIGN") -> DESIGN
                upperPart.contains("PLAN") || upperPart.contains("PM") -> PM
                else -> ADMIN
            }
        }
    }

}