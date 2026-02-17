package com.umc.data.response.organization

import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.home.GisuInfo
import kotlinx.serialization.Serializable

@Serializable
data class GisuInfoResponse(
    val gisuId : Long,
    val generation : Long,
    val startAt : String,
    val endAt : String,
    val isActive : Boolean
) {
    companion object{
        fun GisuInfoResponse.toModel() : GisuInfo {

            val (startDate, startClock) = this.startAt.parseDateTime()
            val (endDate, endClock) = this.endAt.parseDateTime()

            return GisuInfo(
                gisuId = gisuId,
                gisu = generation,
                startAt = startDate,
                endAt = endDate,
                isActive = isActive
            )
        }
    }
}
