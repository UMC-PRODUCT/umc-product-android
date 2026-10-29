package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName
import com.umc.data.response.base.CursorResponse

data class ChallengerSearchV2Response(
    val totalCount: Long = 0,
    val page: ChallengerSearchV2PageResponse? = null
) {
    fun toCursorResponse(): ChallengerCursorResponse {
        val page = page ?: ChallengerSearchV2PageResponse()
        val items = page.content.orEmpty().map {
            ChallengerCursorItemResponse(
                challengerId = it.challengerId,
                memberId = it.memberId,
                gisuId = it.gisuId,
                gisu = it.generation.toInt(),
                part = it.part,
                name = it.name.orEmpty(),
                nickname = it.nickname.orEmpty(),
                schoolName = it.schoolName.orEmpty(),
                pointSum = 0.0,
                profileImageLink = it.profileImageLink,
                roleTypes = it.roleTypes.orEmpty()
            )
        }
        return ChallengerCursorResponse(
            cursor = CursorResponse(
                content = items,
                nextCursor = if (page.hasNext) (page.page + 1).toLong() else null,
                hasNext = page.hasNext
            ),
            partCounts = items.groupingBy { it.part }.eachCount().map { (part, count) ->
                ChallengerPartCountResponse(part, count)
            }
        )
    }
}

data class ChallengerSearchV2PageResponse(
    val content: List<ChallengerSearchV2ItemResponse>? = null,
    val page: Int = 0,
    val hasNext: Boolean = false
)

/**
 * Gson 은 Kotlin 의 null 불가 선언을 지키지 않고 서버가 보낸 null 을 그대로 넣는다.
 * 그래서 서버가 비워 보낼 수 있는 문자열·목록은 null 허용으로 받고, 변환할 때 기본값으로 채운다.
 * (null 불가로 두면 변환 시점의 생성자 검사에서 NPE 가 나 앱이 종료된다)
 */
data class ChallengerSearchV2ItemResponse(
    val memberId: Long,
    val name: String? = null,
    val nickname: String? = null,
    val schoolName: String? = null,
    val profileImageLink: String? = null,
    val challengerId: Long,
    val gisuId: Long,
    // 회원 검색(api/v1/member/search)은 같은 값을 gisu 로 보낸다
    @SerializedName(value = "generation", alternate = ["gisu"])
    val generation: Long,
    val part: String? = null,
    val roleTypes: List<String>? = null
)
