package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.PostScrap

data class PostScrapResponse (
    @SerializedName("scrapped") val scrapped: Boolean,
    @SerializedName("scrapCount") val scrapCount: Int,
) {
    companion object {
        fun PostScrapResponse.toPostScrapDomain(): PostScrap = PostScrap(
            scrapped = this.scrapped,
            scrapCount = this.scrapCount
        )
    }
}