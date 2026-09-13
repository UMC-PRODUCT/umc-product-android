package com.umc.domain.repository.remoteconfig

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.remoteconfig.RemoteNotice

interface RemoteConfigRepository {

    //원격 설정의 화면별 안내 목록
    suspend fun getNotices(): ApiState<List<RemoteNotice>>
}
