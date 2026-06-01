package com.umc.data.api

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.DecideAttendanceRequest
import com.umc.data.request.schedule.ExcuseAttendanceRequest
import com.umc.data.request.schedule.ScheduleAttendanceRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.AttendanceDecisionResponse
import com.umc.data.response.schedule.ScheduleAttendanceHistoryResponse
import com.umc.data.response.schedule.ScheduleCapabilitiesResponse
import com.umc.data.response.schedule.ScheduleMeResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    //일정 생성·수정 권한 조회
    @GET(Endpoints.Schedule.CAPABILITIES)
    suspend fun getScheduleCapabilities(): ApiResponse<ScheduleCapabilitiesResponse>

    //기간별 내 일정 목록 조회 (월별/전체 통합)
    @GET(Endpoints.Schedule.SCHEDULES_ME)
    suspend fun getSchedules(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("isAttendanceRequired") isAttendanceRequired: Boolean
    ): ApiResponse<List<ScheduleMeResponse>>

    //세부 일정 가져오기
    @GET(Endpoints.Schedule.DETAIL_V2)
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<ScheduleMeResponse>

    //일정 출석부 통합 삭제하기
    @DELETE(Endpoints.Schedule.DELETE)
    suspend fun deleteScheduleWithAttendance(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<Unit>


    //일정 생성하기 (v2)
    @POST(Endpoints.Schedule.CREATE_V2)
    suspend fun createSchedule(
        @Body request: CreateScheduleRequest
    ): ApiResponse<Long>

    //일정 수정하기
    @PATCH(Endpoints.Schedule.DETAIL_V2)
    suspend fun updateSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateScheduleRequest
    ) : ApiResponse<Unit>

    // 일정 출석 요청
    @POST(Endpoints.Schedule.ATTENDANCE_REQUEST)
    suspend fun postAttendanceRequest(
        @Path("scheduleId") scheduleId: Long,
        @Body request: ScheduleAttendanceRequest
    ): ApiResponse<Unit>

    // 출석 요청 승인/거절
    @POST(Endpoints.Schedule.ATTENDANCE_DECIDE)
    suspend fun postAttendanceDecide(
        @Path("scheduleId") scheduleId: Long,
        @Body requests: List<DecideAttendanceRequest>
    ): ApiResponse<List<AttendanceDecisionResponse>>

    // 사유 출석 제출
    @POST(Endpoints.Schedule.ATTENDANCE_EXCUSE)
    suspend fun postAttendanceExcuse(
        @Path("scheduleId") scheduleId: Long,
        @Body request: ExcuseAttendanceRequest
    ): ApiResponse<Unit>

    // 출석 이력 조회
    @GET(Endpoints.Schedule.ATTENDANCE_HISTORY)
    suspend fun getAttendanceHistory(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("attendanceStatus") attendanceStatus: String? = null
    ): ApiResponse<List<ScheduleAttendanceHistoryResponse>>

    // 단일 일정 출석 현황 조회
    @GET(Endpoints.Schedule.ATTENDANCE_DETAIL)
    suspend fun getScheduleAttendanceDetail(
        @Path("scheduleId") scheduleId: Long,
        @Query("attendanceStatus") attendanceStatus: String? = null
    ): ApiResponse<ScheduleAttendanceHistoryResponse>
}

