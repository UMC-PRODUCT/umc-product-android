package com.umc.data.response.organization

import com.umc.data.response.organization.GisuItemResponse.Companion.toModel
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.organization.GisuList
import kotlinx.serialization.Serializable

@Serializable
data class GisuListResponse(
    val gisuList: List<GisuItemResponse>
) {
    companion object {
        fun GisuListResponse.toModel(): GisuList = GisuList(
            gisuList = gisuList.map { it.toModel() }
        )
    }
}

@Serializable
data class GisuItemResponse(
    val gisuId: Int,
    val generation: Int,
    val isActive: Boolean = true
) {
    companion object {
        fun GisuItemResponse.toModel(): GisuItem = GisuItem(
            gisuId = gisuId,
            generation = generation,
            isActive = isActive
        )
    }
}