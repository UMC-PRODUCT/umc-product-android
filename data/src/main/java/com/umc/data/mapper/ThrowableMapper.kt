package com.umc.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.domain.model.base.FailState
import com.google.gson.stream.MalformedJsonException
import com.umc.data.dataSource.base.NETWORK_ERROR_MESSAGE
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.NETWORK_ERROR_CODE
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException

private val errorBodyGson = Gson()

// 오류 응답의 result 타입은 성공 응답의 T와 다를 수 있으므로 Any? 로 읽는다.
private val errorBodyType = object : TypeToken<ApiResponse<Any?>>() {}.type

/**
 * 서버 오류 응답 본문을 ApiResponse 형식으로 읽는다. 본문이 없거나 형식이 다르면 null 이다.
 * apiCall·[toFailState]·커뮤니티 스레드 저장소가 같은 방식으로 읽도록 여기 한 곳에 둔다.
 * errorBody 는 한 번만 읽을 수 있으므로 예외 하나에 한 번만 부른다.
 * 본문에 code·message 가 빠져 있으면 Gson 이 null 을 넣으므로 호출부는 `?.` 로 읽고 기본값을 채운다.
 */
fun HttpException.parseErrorBody(): ApiResponse<Any?>? {
    return try {
        val raw = response()?.errorBody()?.string()
        if (raw.isNullOrBlank()) null else errorBodyGson.fromJson<ApiResponse<Any?>>(raw, errorBodyType)
    } catch (e: Exception) {
        null
    }
}

fun Throwable.toFailState(): FailState {
    return when (this) {
        // 응답 본문 변환 실패는 연결 문제가 아니므로 네트워크 오류로 분류하지 않는다.
        is MalformedJsonException, is EOFException -> FailState(
            code = "UNKNOWN",
            message = message ?: "알 수 없는 오류가 발생했습니다."
        )

        is IOException -> FailState(
            code = NETWORK_ERROR_CODE,
            message = NETWORK_ERROR_MESSAGE
        )

        is HttpException -> {
            val httpCode = code()
            val body = parseErrorBody()
            // 본문을 읽었으면 빠진 값만 HTTP 코드로 채우고, 본문이 없으면 예외 메시지를 쓴다.
            val fallbackMessage =
                if (body != null) "HTTP 오류($httpCode)" else message ?: "서버 오류가 발생했습니다. ($httpCode)"
            FailState(
                code = body?.code ?: "HTTP_$httpCode",
                message = body?.message ?: fallbackMessage,
            )
        }

        else -> FailState(
            code = "UNKNOWN",
            message = message ?: "알 수 없는 오류가 발생했습니다."
        )
    }
}
