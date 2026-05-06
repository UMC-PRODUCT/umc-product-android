package com.umc.data.api

import com.umc.data.response.attendance.AdminPendingUserResponse
import com.umc.data.response.attendance.AttendanceAvailableResponse
import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.data.response.attendance.UserCheckHistoryResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.attendance.AttendanceReasonRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AttendanceApi {
    @GET(Endpoints.Attendance.AVAILABLE)
    suspend fun getAttendanceAvailable(): ApiResponse<List<AttendanceAvailableResponse>>

    @GET(Endpoints.Attendance.PENDING)
    suspend fun getPendingUsers(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<List<AdminPendingUserResponse>>

    @POST(Endpoints.Attendance.APPROVE)
    suspend fun approveAttendance(
        @Path("recordId") recordId: Long
    ): ApiResponse<Unit>

    @POST(Endpoints.Attendance.REJECT)
    suspend fun rejectAttendance(
        @Path("recordId") recordId: Long
    ): ApiResponse<Unit>

    @POST(Endpoints.Attendance.REASON)
    suspend fun postAttendanceReason(
        @Body request: AttendanceReasonRequest
    ): ApiResponse<String>

    @GET(Endpoints.Attendance.HISTORY)
    suspend fun getAttendanceHistory(): ApiResponse<List<UserCheckHistoryResponse>>

    @GET(Endpoints.Attendance.CHALLENGER_HISTORY)
    suspend fun getChallengerAttendanceHistory(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<List<ChallengerAttendanceHistoryResponse>>
}