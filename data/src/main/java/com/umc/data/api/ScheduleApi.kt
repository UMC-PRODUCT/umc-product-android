package com.umc.data.api

import com.umc.data.response.member.MemberResponse
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    @GET(Endpoints.Schedule.SCHEDULE)
    suspend fun getScheduleList(): ApiResponse<List<ScheduleListResponse>>

    @GET(Endpoints.Schedule.DETAIL)
    suspend fun getScheduleDetail(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<ScheduleDetailResponse>

    @GET(Endpoints.Schedule.MONTH)
    suspend fun getMonthSchedule(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResponse<List<ScheduleMonthResponse>>

}

