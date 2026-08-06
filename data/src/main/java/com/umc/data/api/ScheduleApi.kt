package com.umc.data.api

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.member.MemberResponse
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.data.response.schedule.AdminScheduleV2Response
import com.umc.data.response.schedule.MyScheduleItemResponse
import com.umc.data.response.schedule.UpdateScheduleLocationV2Request
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

    //일정 목록 가져오기
    @GET(Endpoints.Schedule.ATTENDANCE_HISTORY)
    suspend fun getScheduleList(): ApiResponse<List<AdminScheduleV2Response>>

    //세부 일정 가져오기
    @GET(Endpoints.Schedule.DETAIL_V2)
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<ScheduleDetailResponse>

    //월별 일정 조회하기(나가리)
    /*
    @GET(Endpoints.Schedule.MONTH)
    suspend fun getMonthSchedule(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResponse<List<ScheduleMonthResponse>>

     */

    //내 일정 조회하기(월별 일정 대신 사용) v2
    @GET(Endpoints.Schedule.SCHEDULES_ME)
    suspend fun getMySchedules(
        @Query("from") from: String, // "2026-06-01T00:00:00Z"
        @Query("to") to: String,     // "2026-06-30T23:59:59Z"
        @Query("isAttendanceRequired") isAttendanceRequired: Boolean = false
    ): ApiResponse<List<MyScheduleItemResponse>>





    //일정 출석부 통합 삭제하기
    @DELETE(Endpoints.Schedule.DETAIL_V2)
    suspend fun deleteScheduleWithAttendance(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<Unit>

    // 출석 기록이 있는 일정 강제 삭제하기 (SUPER_ADMIN 전용)
    @DELETE(Endpoints.Schedule.FORCE_DELETE)
    suspend fun forceDeleteSchedule(
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
    ) : ApiResponse<String>

    // 일정 위치 변경하기
    @PATCH(Endpoints.Schedule.DETAIL_V2)
    suspend fun updateScheduleLocation(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateScheduleLocationV2Request
    ): ApiResponse<String>


    // 스터디 그룹 일정 생성
    @POST(Endpoints.Schedule.CREATE_STUDY_GROUP_SCHEDULE)
    suspend fun createStudyGroupSchedule(
        @Body request: CreateStudyGroupScheduleRequest
    ): ApiResponse<Long>
}

