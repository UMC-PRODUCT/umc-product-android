package com.umc.data.response.challenger

import com.umc.data.response.base.CursorResponse

data class ChallengerSearchV2Response(
    val totalCount: Long = 0,
    val page: ChallengerSearchV2PageResponse
) {
    fun toCursorResponse(): ChallengerCursorResponse {
        val items = page.content.map {
            ChallengerCursorItemResponse(
                challengerId = it.challengerId,
                memberId = it.memberId,
                gisuId = it.gisuId,
                gisu = it.generation.toInt(),
                part = it.part,
                name = it.name,
                nickname = it.nickname,
                schoolName = it.schoolName,
                pointSum = 0.0,
                profileImageLink = it.profileImageLink,
                roleTypes = it.roleTypes
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
    val content: List<ChallengerSearchV2ItemResponse> = emptyList(),
    val page: Int = 0,
    val hasNext: Boolean = false
)

data class ChallengerSearchV2ItemResponse(
    val memberId: Long,
    val name: String,
    val nickname: String,
    val schoolName: String,
    val profileImageLink: String? = null,
    val challengerId: Long,
    val gisuId: Long,
    val generation: Long,
    val part: String,
    val roleTypes: List<String> = emptyList()
)
