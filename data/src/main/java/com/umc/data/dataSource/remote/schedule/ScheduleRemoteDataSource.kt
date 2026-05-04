package com.umc.data.dataSource.remote.schedule

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleMeResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.schedule.UpdateLocationRequest

interface ScheduleRemoteDataSource {

    //기간별 내 일정 목록 조회
    suspend fun getSchedules(from: String, to: String, isAttendanceRequired: Boolean): ApiState<List<ScheduleMeResponse>>

    //일정 상세정보 가져오기
    suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleMeResponse>

    //일정 삭제하기
    suspend fun deleteScheduleWithAttendance(scheduleId: Long): ApiState<Unit>

    //일정 생성하기
    suspend fun createScheduleWithAttendance(request: CreateScheduleRequest): ApiState<String>

    //일정 수정하기
    suspend fun updateSchedule(scheduleId: Long, request: UpdateScheduleRequest): ApiState<Unit>

    // 위치 변경하기
    suspend fun updateScheduleLocation(scheduleId: Long, request: UpdateLocationRequest): ApiState<UpdateLocationResponse>


    // 스터디 그룹 일정 만들기
    suspend fun createStudyGroupSchedule(request: CreateStudyGroupScheduleRequest): ApiState<Long>
}

