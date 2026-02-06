package com.umc.data.api

import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET

interface AttendanceApi {
    @GET(Endpoints.Attendance.AVAILABLE)
    suspend fun getAttendanceAvailable(): ApiResponse<List<AttendanceAvailableResponse>>
}