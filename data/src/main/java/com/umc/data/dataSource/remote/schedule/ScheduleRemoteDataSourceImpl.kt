package com.umc.data.dataSource.remote.schedule

import com.umc.data.api.ScheduleApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class ScheduleRemoteDataSourceImpl @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ScheduleRemoteDataSource {

    override suspend fun getScheduleList(): ApiState<List<ScheduleListResponse>> {
        return apiCall {scheduleApi.getScheduleList()}
    }

    override suspend fun getScheduleDetail(scheduleId: Int): ApiState<ScheduleDetailResponse> {
        return apiCall {scheduleApi.getScheduleDetail(scheduleId)}
    }

    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthResponse>> {
        return apiCall {scheduleApi.getMonthSchedule(year, month)}
    }
}