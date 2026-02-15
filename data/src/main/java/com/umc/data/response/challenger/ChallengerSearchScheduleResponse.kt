package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName
import com.umc.data.response.challenger.ChallengerItemResponse.Companion.toParticipantItem
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.ParticipantSearchPage


//챌린저 검색(일정 생성 용도로 할 때의 결과)
data class ChallengerSearchScheduleResponse (
    @SerializedName("cursor") val cursor: ChallengerCursorData
) {
    companion object {
        fun ChallengerSearchScheduleResponse.toParticipantSearchPage(): ParticipantSearchPage {
            return ParticipantSearchPage(
                content = this.cursor.content.map { it.toParticipantItem() },
                nextCursor = this.cursor.nextCursor,
                hasNext = this.cursor.hasNext
            )
        }
    }
}

data class ChallengerCursorData(
    @SerializedName("content") val content: List<ChallengerItemResponse>,
    @SerializedName("nextCursor") val nextCursor: Long?,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class ChallengerItemResponse(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("name") val name: String,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("gisu") val gisu: Long,
    @SerializedName("part") val part: String,
    @SerializedName("profileImageLink") val profileImageLink: String
) {
    companion object {
        fun ChallengerItemResponse.toParticipantItem(): ParticipantItem {
            return ParticipantItem(
                id = this.memberId,
                name = this.name,
                nickname = this.nickname,
                school = this.schoolName,
                gisu = this.gisu,
                userPart = mapToUserPart(this.part),
                profileImage = this.profileImageLink
            )
        }
    }
}



// 매핑 함수
private fun mapToUserPart(partStr: String): UserPart {
    return try {
        // 대문자 변환 및 언더바 제거 (SPRING_BOOT -> SPRINGBOOT 대응)
        val sanitizedPart = partStr.uppercase().replace("_", "")

        // Enum 이름과 일치하는 요소 찾기
        UserPart.valueOf(sanitizedPart)
    } catch (e: Exception) {
        // 매칭되는 파트가 없으면 기본값으로 ANDROID 설정 (혹은 로그 출력)
        UserPart.ANDROID
    }
}