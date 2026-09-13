package com.umc.domain.usecase.remoteconfig

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.remoteconfig.RemoteNotice
import com.umc.domain.repository.remoteconfig.RemoteConfigRepository
import javax.inject.Inject

class GetRemoteNoticesUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
) {
    suspend operator fun invoke(): ApiState<List<RemoteNotice>> {
        return remoteConfigRepository.getNotices()
    }
}
