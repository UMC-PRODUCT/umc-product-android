package com.umc.data.dataSource.remote.remoteconfig

import com.umc.data.api.RemoteConfigApi
import com.umc.data.response.remoteconfig.AppConfigResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class RemoteConfigRemoteDataSourceImpl @Inject constructor(
    private val remoteConfigApi: RemoteConfigApi
) : RemoteConfigRemoteDataSource {

    override suspend fun getAppConfig(): ApiState<AppConfigResponse> {
        return try {
            ApiState.Success(remoteConfigApi.getAppConfig(cacheControl = null))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 네트워크가 안 되면 마지막으로 받아둔 설정이라도 쓴다
            fromCache() ?: ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }

    private suspend fun fromCache(): ApiState<AppConfigResponse>? {
        return try {
            ApiState.Success(remoteConfigApi.getAppConfig(cacheControl = FORCE_CACHE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        // 기한이 지난 캐시라도 네트워크 없이 캐시에서만 읽는다. 캐시가 없으면 실패한다
        private const val FORCE_CACHE = "only-if-cached, max-stale=2147483647"
    }
}
