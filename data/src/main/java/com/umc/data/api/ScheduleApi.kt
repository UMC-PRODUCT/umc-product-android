package com.umc.data.api

import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.member.MemberResponse
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    //일정 목록 가져오기
    @GET(Endpoints.Schedule.SCHEDULE)
    suspend fun getScheduleList(): ApiResponse<List<ScheduleListResponse>>

    //세부 일정 가져오기
    @GET(Endpoints.Schedule.DETAIL)
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<ScheduleDetailResponse>

    //월별 일정 조회하기
    @GET(Endpoints.Schedule.MONTH)
    suspend fun getMonthSchedule(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResponse<List<ScheduleMonthResponse>>

    //일정 출석부 통합 삭제하기
    @DELETE(Endpoints.Schedule.DELETE)
    suspend fun deleteScheduleWithAttendance(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<Unit>


    //일정 출석부 통합 생성하기
    /**response로 일정 id가 string으로 오니 Long으로 변환**/
    @POST(Endpoints.Schedule.SCHEDULE_WITH_ATTENDANCE)
    suspend fun createScheduleWithAttendance(
        @Body request: CreateScheduleRequest
    ): ApiResponse<String>

    //일정 수정하기
    @PATCH(Endpoints.Schedule.DETAIL)
    suspend fun updateSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateScheduleRequest
    ) : ApiResponse<Unit>



}

