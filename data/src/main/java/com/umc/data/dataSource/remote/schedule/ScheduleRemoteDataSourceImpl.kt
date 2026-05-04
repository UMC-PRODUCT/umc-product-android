package com.umc.data.dataSource.remote.schedule

import com.umc.data.api.ScheduleApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleMeResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.schedule.UpdateLocationRequest
import javax.inject.Inject

class ScheduleRemoteDataSourceImpl @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ScheduleRemoteDataSource {

    //기간별 내 일정 조회
    override suspend fun getSchedules(
        from: String,
        to: String,
        isAttendanceRequired: Boolean
    ): ApiState<List<ScheduleMeResponse>> {
        return apiCall { scheduleApi.getSchedules(from, to, isAttendanceRequired) }
    }

    //일정 상세 조회
    override suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleDetailResponse> {
        return apiCall {scheduleApi.getScheduleDetail(scheduleId)}
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

    // 위치 변경하기
    override suspend fun updateScheduleLocation(
        scheduleId: Long,
        request: UpdateLocationRequest
    ): ApiState<UpdateLocationResponse> {
        return apiCall { scheduleApi.updateScheduleLocation(scheduleId, request) }
    }


    override suspend fun createStudyGroupSchedule(
        request: CreateStudyGroupScheduleRequest
    ): ApiState<Long> {
        return apiCall { scheduleApi.createStudyGroupSchedule(request) }
    }

}