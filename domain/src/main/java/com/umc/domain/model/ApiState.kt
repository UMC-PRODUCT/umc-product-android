package com.umc.domain.model

sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()

    data class Success<T>(val result: T) : ApiState<T>()

    data class Error<T>(val result: T?, val errorCode: String) : ApiState<T>()
}
