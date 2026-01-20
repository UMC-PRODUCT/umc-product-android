package com.umc.data.dto.response

import com.umc.domain.model.Model
import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationCompleteResponse(
    val emailVerificationToken: String
) : Response {
    override fun toModel(): Model {
        TODO("Not yet implemented")
    }
}
