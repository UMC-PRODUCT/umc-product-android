package com.umc.data.dataSource.base

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.NETWORK_ERROR_CODE
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException

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

    } catch (e: HttpException) {
        //TODO 공통 에러
        val errorBodyStr = e.response()?.errorBody()?.string()
        // 오류 응답의 result 타입은 성공 응답의 T와 다를 수 있으므로
        // 메시지 파싱이 실패하지 않도록 Any?로 분리해서 읽는다.
        val type = object : TypeToken<ApiResponse<Any?>>() {}.type

        val errorResponse: ApiResponse<Any?>? = try {
            Gson().fromJson(errorBodyStr, type)
        } catch (parseException: Exception) {
            null
        }

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
