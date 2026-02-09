package com.umc.data.dataSource.remote.schedule

import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiState

interface ScheduleRemoteDataSource {
    suspend fun getScheduleList(): ApiState<List<ScheduleListResponse>>

    suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleDetailResponse>

    suspend fun getMonthSchedule(year: Int, month: Int): ApiState<List<ScheduleMonthResponse>>

}