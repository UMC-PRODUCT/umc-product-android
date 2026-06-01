package com.umc.data.api

import com.umc.data.response.attendance.ChallengerAttendanceHistoryResponse
import com.umc.domain.model.base.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AttendanceApi {
    @GET(Endpoints.Attendance.CHALLENGER_HISTORY)
    suspend fun getChallengerAttendanceHistory(
        @Path("challengerId") challengerId: Long
    ): ApiResponse<List<ChallengerAttendanceHistoryResponse>>
}