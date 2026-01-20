package com.umc.data.base

import com.google.gson.annotations.SerializedName

data class ApiResponse<Vo>(
    @SerializedName("isSuccess")
    val isSuccess: Boolean = false,
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("result")
    val result: Vo? = null,
)
