package com.umc.data.dataSource.remote.remoteconfig

import com.umc.data.response.remoteconfig.AppConfigResponse
import com.umc.domain.model.base.ApiState

interface RemoteConfigRemoteDataSource {

    //원격 설정 파일 조회
    suspend fun getAppConfig(): ApiState<AppConfigResponse>
}
