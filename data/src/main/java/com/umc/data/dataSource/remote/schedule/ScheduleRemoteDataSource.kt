package com.umc.data.dataSource.remote.schedule

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.DecideAttendanceRequest
import com.umc.data.request.schedule.ExcuseAttendanceRequest
import com.umc.data.request.schedule.ScheduleAttendanceRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleAttendanceHistoryResponse
import com.umc.data.response.schedule.ScheduleCapabilitiesResponse
import com.umc.data.response.schedule.ScheduleMeResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.schedule.UpdateLocationRequest

interface ScheduleRemoteDataSource {

    //일정 생성·수정 권한 조회
    suspend fun getScheduleCapabilities(): ApiState<ScheduleCapabilitiesResponse>

    //기간별 내 일정 목록 조회
    suspend fun getSchedules(from: String, to: String, isAttendanceRequired: Boolean): ApiState<List<ScheduleMeResponse>>

    //일정 상세정보 가져오기
    suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleMeResponse>

    //일정 삭제하기
    suspend fun deleteScheduleWithAttendance(scheduleId: Long): ApiState<Unit>

    //일정 생성하기
    suspend fun createSchedule(request: CreateScheduleRequest): ApiState<Long>

    //일정 수정하기
    suspend fun updateSchedule(scheduleId: Long, request: UpdateScheduleRequest): ApiState<Unit>

    // 위치 변경하기
    suspend fun updateScheduleLocation(scheduleId: Long, request: UpdateLocationRequest): ApiState<UpdateLocationResponse>


    // 스터디 그룹 일정 만들기
    suspend fun createStudyGroupSchedule(request: CreateStudyGroupScheduleRequest): ApiState<Long>

    // 일정 출석 요청
    suspend fun postAttendanceRequest(scheduleId: Long, request: ScheduleAttendanceRequest): ApiState<Unit>

    // 출석 요청 승인/거절
    suspend fun postAttendanceDecide(scheduleId: Long, requests: List<DecideAttendanceRequest>): ApiState<Unit>

    // 사유 출석 제출
    suspend fun postAttendanceExcuse(scheduleId: Long, request: ExcuseAttendanceRequest): ApiState<Unit>

    // 출석 이력 조회
    suspend fun getAttendanceHistory(from: String?, to: String?, attendanceStatus: String?): ApiState<List<ScheduleAttendanceHistoryResponse>>
}

