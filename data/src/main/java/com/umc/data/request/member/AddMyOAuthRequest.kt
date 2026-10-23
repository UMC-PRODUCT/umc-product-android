package com.umc.data.request.member

import com.google.gson.annotations.SerializedName

data class AddMyOAuthRequest(
    @SerializedName("oAuthVerificationToken")
    val oAuthVerificationToken: String
)
