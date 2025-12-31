package com.umc.data.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

abstract class BaseDataSource {

     inline fun <reified D> apiLaunch(
         crossinline apiCall: suspend () -> ApiResponse<D>,
    ): Flow<ApiState<D?>> = flow {
        val response = apiCall()
        when (response.isSuccess) {
            true -> {
                val apiSuccess = ApiState.Success(response.result)
                emit(apiSuccess)
            }
            false -> {
                val apiError = ApiState.Error(response.result, errorCode = response.code)
                emit(apiError)
            }
        }
    }.onStart { emit(ApiState.Loading) }.catch { e: Throwable ->
         e.printStackTrace()
         emit(ApiState.Error(null, "400"))
     }
}