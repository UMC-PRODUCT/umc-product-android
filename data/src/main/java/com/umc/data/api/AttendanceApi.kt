package com.umc.data.api

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.data.response.attendance.UserCheckHistoryResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AttendanceApi {
    @GET(Endpoints.Attendance.AVAILABLE)
    suspend fun getAttendanceAvailable(): ApiResponse<List<AttendanceAvailableResponse>>

    @GET(Endpoints.Attendance.PENDING)
    suspend fun getPendingUsers(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<List<AdminPendingUserResponse>>

    @GET(Endpoints.Attendance.HISTORY)
    suspend fun getAttendanceHistory(): ApiResponse<List<UserCheckHistoryResponse>>

    @GET(Endpoints.Attendance.CHALLENGER_HISTORY)
    suspend fun getChallengerAttendanceHistory(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<List<ChallengerAttendanceHistoryResponse>>
}