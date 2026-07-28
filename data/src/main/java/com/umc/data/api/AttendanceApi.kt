package com.umc.data.api

import com.umc.data.response.attendance.AdminScheduleAttendanceV2Response
import com.umc.data.response.attendance.AttendanceDecisionV2Request
import com.umc.data.response.attendance.AttendanceCheckV2Request
import com.umc.data.response.attendance.AttendanceExcuseV2Request
import com.umc.data.response.attendance.ScheduleAttendanceV2Response
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApi {
    @GET(Endpoints.Attendance.AVAILABLE)
    suspend fun getAttendanceAvailable(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("isAttendanceRequired") isAttendanceRequired: Boolean = true
    ): ApiResponse<List<ScheduleAttendanceV2Response>>

    @POST(Endpoints.Attendance.CHECK)
    suspend fun postAttendanceCheck(
        @Path("scheduleId") scheduleId: Long,
        @Body request: AttendanceCheckV2Request
    ): ApiResponse<Unit>

    @GET(Endpoints.Attendance.PENDING)
    suspend fun getPendingUsers(
        @Path("scheduleId") scheduleId: Long
    ): ApiResponse<AdminScheduleAttendanceV2Response>

    @POST(Endpoints.Attendance.DECIDE)
    suspend fun decideAttendance(
        @Path("scheduleId") scheduleId: Long,
        @Body requests: List<AttendanceDecisionV2Request>
    ): ApiResponse<Unit>

    @POST(Endpoints.Attendance.REASON)
    suspend fun postAttendanceReason(
        @Path("scheduleId") scheduleId: Long,
        @Body request: AttendanceExcuseV2Request
    ): ApiResponse<Unit>

    @GET(Endpoints.Attendance.HISTORY)
    suspend fun getAttendanceHistory(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("isAttendanceRequired") isAttendanceRequired: Boolean = true
    ): ApiResponse<List<ScheduleAttendanceV2Response>>
}
