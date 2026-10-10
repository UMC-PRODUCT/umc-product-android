package com.umc.data.mapper

import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.NETWORK_ERROR_CODE
import com.umc.domain.model.base.NETWORK_ERROR_MESSAGE
import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject

fun Throwable.toFailState(): FailState {
    return when (this) {
        is IOException -> FailState(
            code = NETWORK_ERROR_CODE,
            message = NETWORK_ERROR_MESSAGE
        )

        is HttpException -> {
            val httpCode = code()


            val parsed = runCatching {
                val raw = response()?.errorBody()?.string().orEmpty()
                if (raw.isBlank()) return@runCatching null
                val json = JSONObject(raw)
                FailState(
                    code = json.optString("code", "HTTP_$httpCode"),
                    message = json.optString("message", "HTTP 오류($httpCode)")
                )
            }.getOrNull()

            parsed ?: FailState(
                code = "HTTP_$httpCode",
                message = message ?: "서버 오류가 발생했습니다. ($httpCode)"
            )
        }

        else -> FailState(
            code = "UNKNOWN",
            message = message ?: "알 수 없는 오류가 발생했습니다."
        )
    }
}
