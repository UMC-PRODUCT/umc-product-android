package com.umc.data.repository.schedule

import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.response.schedule.ScheduleDetailResponse.Companion.toModel
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource
) : ScheduleRepository {

    override suspend fun getScheduleDetail(scheduleId: Int): ApiState<UserCheckAvailable> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { response ->
            response.toModel()
        }
    }
}