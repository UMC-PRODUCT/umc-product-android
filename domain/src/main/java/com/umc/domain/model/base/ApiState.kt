package com.umc.domain.model.base

import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

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

/**
 * [map] 과 같지만 변환 중 예외가 나면 앱을 종료시키지 않고 실패로 돌려준다.
 * 서버 응답이 약속과 다를 때(필수 값이 비어 옴 등) 화면이 오류 상태로 처리하게 하려고 쓴다.
 * inline 이라 suspend 함수 안에서는 변환 람다도 일시 중단될 수 있다. 그때 나는 취소 예외는
 * 실패로 바꾸지 않고 그대로 다시 던져 코루틴 취소를 막지 않는다.
 */
inline fun <S, T> ApiState<T>.mapCatching(function: (T) -> S): ApiState<S> {
    return when (this) {
        is ApiState.Success -> try {
            ApiState.Success(function(this.data))
        } catch (e: CancellationException) {
            // CancellationException 도 RuntimeException 의 하위라 아래 catch 보다 먼저 걸러야 한다
            throw e
        } catch (e: RuntimeException) {
            e.printStackTrace()
            ApiState.Fail(
                FailState(
                    code = RESPONSE_MAPPING_ERROR_CODE,
                    message = RESPONSE_MAPPING_ERROR_MESSAGE,
                )
            )
        }
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

/** 응답은 받았지만 화면용 모델로 바꾸다 실패했을 때([mapCatching]) FailState.code 로 쓰는 값. */
const val RESPONSE_MAPPING_ERROR_CODE = "RESPONSE_MAPPING_ERROR"

const val RESPONSE_MAPPING_ERROR_MESSAGE = "정보를 불러오지 못했어요. 잠시 후 다시 시도해주세요."

/** 서버 응답 없이 네트워크 단절로 실패한 경우인지 여부. 서버/인증 실패와 구분할 때 쓴다. */
val FailState.isNetworkError: Boolean
    get() = code == NETWORK_ERROR_CODE
