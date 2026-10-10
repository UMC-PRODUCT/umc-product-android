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

/** 네트워크 단절(IOException 계열)로 요청 자체가 실패했을 때 FailState.code 로 쓰는 값. */
const val NETWORK_ERROR_CODE = "NETWORK_ERROR"

/** 서버 응답 없이 네트워크 단절로 실패한 경우인지 여부. 서버/인증 실패와 구분할 때 쓴다. */
val FailState.isNetworkError: Boolean
    get() = code == NETWORK_ERROR_CODE
