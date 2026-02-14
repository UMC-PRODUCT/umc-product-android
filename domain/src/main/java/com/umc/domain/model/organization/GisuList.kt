package com.umc.domain.model.organization

data class GisuList(
    val gisuList: List<GisuItem> = emptyList()
)

data class GisuItem(
    val gisuId: Int = -1,
    val generation: Int = 0,
    val isActive: Boolean = false
)