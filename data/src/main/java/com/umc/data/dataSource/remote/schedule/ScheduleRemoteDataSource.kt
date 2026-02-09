package com.umc.data.dataSource.remote.schedule

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiState

interface ScheduleRemoteDataSource {

    //일정 목록 가져오기
    suspend fun getScheduleList(): ApiState<List<ScheduleListResponse>>

    //일정 상세정보 가져오기
    suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleDetailResponse>

    //월별 일정 가져오기
    suspend fun getMonthSchedule(year: Int, month: Int): ApiState<List<ScheduleMonthResponse>>

    //일정 삭제하기
    suspend fun deleteScheduleWithAttendance(scheduleId: Long): ApiState<Unit>

    //일정 생성하기
    suspend fun createScheduleWithAttendance(request: CreateScheduleRequest): ApiState<String>

    //일정 수정하기
    suspend fun updateSchedule(scheduleId: Long, request: UpdateScheduleRequest): ApiState<Unit>
}

