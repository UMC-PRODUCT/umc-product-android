package com.umc.domain.model.base

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val isSuccess: Boolean = false,
    val code: String,
    val message: String,
    val result: T? = null,
)
