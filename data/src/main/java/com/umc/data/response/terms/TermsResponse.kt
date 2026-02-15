package com.umc.data.response.terms

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.terms.TermsData

data class TermsResponse (
    @SerializedName("id") val id: Long,
    @SerializedName("link") val link: String,
    @SerializedName("isMandatory") val isMandatory: Boolean,
) {
    companion object {
        fun TermsResponse.toDomain(): TermsData = TermsData(
            id = this.id,
            link = this.link,
            isMandatory = this.isMandatory
        )
    }
}