package com.umc.data.repository.remoteconfig

import com.umc.data.dataSource.remote.remoteconfig.RemoteConfigRemoteDataSource
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.remoteconfig.RemoteNotice
import com.umc.domain.repository.remoteconfig.RemoteConfigRepository
import javax.inject.Inject

class RemoteConfigRepositoryImpl @Inject constructor(
    private val remoteConfigRemoteDataSource: RemoteConfigRemoteDataSource
) : RemoteConfigRepository {

    override suspend fun getNotices(): ApiState<List<RemoteNotice>> {
        return remoteConfigRemoteDataSource.getAppConfig().map { it.toDomain() }
    }
}
