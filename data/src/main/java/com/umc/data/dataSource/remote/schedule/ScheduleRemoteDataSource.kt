package com.umc.data.dataSource.remote.schedule

import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.domain.model.base.ApiState

interface ScheduleRemoteDataSource {
    suspend fun getScheduleDetail(scheduleId: Int): ApiState<ScheduleDetailResponse>
}