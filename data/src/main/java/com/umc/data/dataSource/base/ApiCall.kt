package com.umc.data.dataSource.base

import com.umc.data.mapper.parseErrorBody
import com.umc.data.mapper.toFailState
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.NETWORK_ERROR_CODE
import com.umc.domain.model.base.RESPONSE_MAPPING_ERROR_CODE
import com.umc.domain.model.base.RESPONSE_MAPPING_ERROR_MESSAGE
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// inline 함수에서 쓰므로 public 으로 둔다.
const val NETWORK_ERROR_MESSAGE = "네트워크 연결을 확인하고 다시 시도해주세요."

suspend inline fun <reified T> apiCall(
    crossinline apiCall: suspend () -> ApiResponse<T>
): ApiState<T> {
    return try {
        val response = apiCall()

        if (response.success) {
            ApiState.Success(response.result)
        } else {
            ApiState.Fail(
                FailState(success = false, code = response.code, message = response.message)
            )
        }

    } catch (e: CancellationException) {
        // 취소는 실패가 아니다. 아래 Exception 에서 잡으면 취소된 요청이 Fail 로 돌아와
        // 화면이 오류 토스트를 띄우거나 이전 조건의 결과로 상태를 덮어쓴다.
        throw e

    } catch (e: HttpException) {
        val errorResponse = e.parseErrorBody()

        val code = errorResponse?.code ?: e.code().toString()
        val message = errorResponse?.message ?: e.message()

        ApiState.Fail(FailState(success = false, code = code, message = message))

    } catch (e: MalformedJsonException) {
        // 응답 본문 변환 실패는 연결 문제가 아니라 서버 응답 계약 문제이므로 네트워크 오류로 분류하지 않는다.
        e.printStackTrace()
        ApiState.Fail(
            FailState(success = false, code = "400", message = e.message ?: "Unknown Error")
        )

    } catch (e: EOFException) {
        // 2xx 빈 본문 등 응답 변환 실패도 위와 같이 일반 오류로 처리한다.
        e.printStackTrace()
        ApiState.Fail(
            FailState(success = false, code = "400", message = e.message ?: "Unknown Error")
        )

    } catch (e: IOException) {
        e.printStackTrace()
        ApiState.Fail(
            FailState(success = false, code = NETWORK_ERROR_CODE, message = NETWORK_ERROR_MESSAGE)
        )

    } catch (e: Exception) {
        e.printStackTrace()
        ApiState.Fail(
            FailState(success = false, code = "400", message = e.message ?: "Unknown Error")
        )
    } as ApiState<T>
}

/**
 * ApiResponse 래퍼가 없는 외부 API(카카오 등)를 호출해 ApiState 로 감싼다.
 * 취소는 다시 던지고, 그 밖의 예외는 [toFailState] 로 분류해 네트워크 단절을 NETWORK_ERROR 로 돌려준다.
 */
suspend fun <T> externalApiCall(call: suspend () -> T): ApiState<T> {
    return try {
        ApiState.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiState.Fail(e.toFailState())
    }
}

/**
 * 성공 응답인데 result 가 비어 온 경우를 실패로 돌린다.
 * apiCall 은 result 가 null 이어도 Success 로 감싸므로, 값을 바로 화면 모델로 바꾸는 호출부가 NPE 로 종료되지 않게 한다.
 */
fun <T> ApiState<T>.requireResult(): ApiState<T> {
    return if (this is ApiState.Success && data == null) {
        ApiState.Fail(FailState(code = RESPONSE_MAPPING_ERROR_CODE, message = RESPONSE_MAPPING_ERROR_MESSAGE))
    } else {
        this
    }
}
