package com.umc.data.dataSource.remote.schedule

import com.umc.data.api.ScheduleApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class ScheduleRemoteDataSourceImpl @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ScheduleRemoteDataSource {

    //일정 리스트 조회
    override suspend fun getScheduleList(): ApiState<List<ScheduleListResponse>> {
        return apiCall {scheduleApi.getScheduleList()}
    }

    //일정 상세 조회
    override suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleDetailResponse> {
        return apiCall {scheduleApi.getScheduleDetail(scheduleId)}
    }

    //워별 일정 조회
    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthResponse>> {
        return apiCall {scheduleApi.getMonthSchedule(year, month)}
    }

    //일정 삭제하기
    override suspend fun deleteScheduleWithAttendance(scheduleId: Long): ApiState<Unit> {
        return apiCall {scheduleApi.deleteScheduleWithAttendance(scheduleId)}
    }

    //일정 생성하기
    override suspend fun createScheduleWithAttendance(request: CreateScheduleRequest): ApiState<String> {
        return apiCall {scheduleApi.createScheduleWithAttendance(request)}
    }

    //일정 수정하기
    override suspend fun updateSchedule(
        scheduleId: Long,
        request: UpdateScheduleRequest
    ): ApiState<Unit> {
        return apiCall {scheduleApi.updateSchedule(scheduleId, request)}
    }

}