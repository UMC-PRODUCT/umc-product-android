package com.umc.data.api

import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AttendanceApi {
    @GET(Endpoints.Attendance.AVAILABLE)
    suspend fun getAttendanceAvailable(): ApiResponse<List<AttendanceAvailableResponse>>

    @POST(Endpoints.Attendance.CHECK)
    suspend fun postAttendanceCheck(
        @Body request: AttendanceCheckRequest
    ): ApiResponse<String>
}