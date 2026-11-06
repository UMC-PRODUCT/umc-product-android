package com.umc.data.response.organization

/** GET /api/v1/gisu 페이지 응답. 다른 화면의 기존 /all 호출은 변경하지 않는다. */
data class GisuPageResponse(
    val content: List<GisuItemResponse>,
    val page: Int,
    val hasNext: Boolean,
)
