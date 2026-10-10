package com.umc.domain.model.base

import kotlinx.serialization.Serializable
@Serializable
sealed interface ApiState<out T> {
    data class Success<T>(val data: T): ApiState<T>
    data class Fail(val failState: FailState): ApiState<Nothing>

}

fun <S, T> ApiState<T>.map(function: (T) -> S) : ApiState<S> {
    return when (this) {
        is ApiState.Success -> ApiState.Success(function(this.data))
        is ApiState.Fail -> this
    }
}

fun <T> ApiState<ApiResponse<T>>.mapSuccessData() : ApiState<T> {
    return when (this) {
        is ApiState.Success -> ApiState.Success(this.data.result ?: (Unit as T))
        is ApiState.Fail -> this
    }
}

data class FailState(
    val success: Boolean = false,
    val code: String,
    val message: String,
) {
    companion object {
        private const val EMPTY = ""
        val default = FailState(
            success = DEFAULT_ERROR,
            code = EMPTY,
            message = EMPTY
        )
    }
}

const val DEFAULT_ERROR = false

const val NETWORK_ERROR_CODE = "NETWORK_ERROR"
const val NETWORK_ERROR_MESSAGE = "네트워크 연결을 확인하고 다시 시도해주세요."

val FailState.isNetworkError: Boolean
    get() = code == NETWORK_ERROR_CODE