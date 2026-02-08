package com.umc.data.dataSource.base

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import retrofit2.HttpException

suspend inline fun <reified T> apiCall(
    crossinline apiCall: suspend () -> ApiResponse<T>
): ApiState<T> {
    return try {
        val response = apiCall()

        if (response.success) {
            ApiState.Success(response.result)
        } else {
            ApiState.Fail(
                FailState(success = false, code = response.code, message = "null")
            )
        }

    } catch (e: HttpException) {
        //TODO 공통 에러
        val errorBodyStr = e.response()?.errorBody()?.string()
        val type = object : TypeToken<ApiResponse<T>>() {}.type

        val errorResponse: ApiResponse<T>? = try {
            Gson().fromJson(errorBodyStr, type)
        } catch (parseException: Exception) {
            null
        }

        val code = errorResponse?.code ?: e.code().toString()
        val message = errorResponse?.message ?: e.message()

        ApiState.Fail(FailState(success = false, code = code, message = message))

    } catch (e: Exception) {
        e.printStackTrace()
        ApiState.Fail(
            FailState(success = false, code = "400", message = e.message ?: "Unknown Error")
        )
    } as ApiState<T>
}