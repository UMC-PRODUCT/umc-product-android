package com.umc.data.api

import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ScheduleApi {
    @GET(Endpoints.Schedule.DETAIL)
    suspend fun getScheduleDetail(@Path("scheduleId") scheduleId: Int): ApiResponse<ScheduleDetailResponse>
}