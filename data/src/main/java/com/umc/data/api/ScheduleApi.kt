package com.umc.data.api

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleCapabilitiesResponse
import com.umc.data.response.schedule.ScheduleMeResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.schedule.UpdateLocationRequest
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
    @PATCH(Endpoints.Schedule.DETAIL)
    suspend fun updateSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateScheduleRequest
    ) : ApiResponse<Unit>

    // 일정 위치 변경하기
    @PATCH(Endpoints.Schedule.LOCATION)
    suspend fun updateScheduleLocation(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateLocationRequest
    ): ApiResponse<UpdateLocationResponse>


    // 스터디 그룹 일정 생성
    @POST(Endpoints.Schedule.CREATE_STUDY_GROUP_SCHEDULE)
    suspend fun createStudyGroupSchedule(
        @Body request: CreateStudyGroupScheduleRequest
    ): ApiResponse<Long>
}

