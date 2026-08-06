package com.umc.data.dataSource.remote.schedule

import com.umc.data.api.ScheduleApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.MyScheduleItemResponse
import com.umc.data.response.schedule.ScheduleDetailResponse
import com.umc.data.response.schedule.ScheduleListResponse
import com.umc.data.response.schedule.ScheduleMonthResponse
import com.umc.data.response.schedule.UpdateLocationResponse
import com.umc.data.response.schedule.ScheduleLocationV2Response
import com.umc.data.response.schedule.UpdateScheduleLocationV2Request
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.schedule.UpdateLocationRequest
import javax.inject.Inject

class ScheduleRemoteDataSourceImpl @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ScheduleRemoteDataSource {

    //일정 리스트 조회
    override suspend fun getScheduleList(): ApiState<List<ScheduleListResponse>> {
        return apiCall { scheduleApi.getScheduleList() }.map { schedules ->
            schedules.map { it.toLegacy() }
        }
    }

    //일정 상세 조회
    override suspend fun getScheduleDetail(scheduleId: Long): ApiState<ScheduleDetailResponse> {
        return apiCall {scheduleApi.getScheduleDetail(scheduleId)}
    }

    //워별 일정 조회
    /*
    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthResponse>> {
        return apiCall {scheduleApi.getMonthSchedule(year, month)}
    }

     */

    //내 일정 조회
    override suspend fun getMySchedule(
        from: String,
        to: String,
        isAttendanceRequired: Boolean
    ): ApiState<List<MyScheduleItemResponse>> {
        return apiCall { scheduleApi.getMySchedules(from, to, isAttendanceRequired) }
    }

    //일정 삭제하기
    override suspend fun deleteScheduleWithAttendance(scheduleId: Long): ApiState<Unit> {
        return apiCall {scheduleApi.deleteScheduleWithAttendance(scheduleId)}
    }

    override suspend fun forceDeleteSchedule(scheduleId: Long): ApiState<Unit> {
        return apiCall { scheduleApi.forceDeleteSchedule(scheduleId) }
    }

    //일정 생성하기
    override suspend fun createSchedule(request: CreateScheduleRequest): ApiState<Long> {
        return apiCall {scheduleApi.createSchedule(request)}
    }

    //일정 수정하기
    override suspend fun updateSchedule(
        scheduleId: Long,
        request: UpdateScheduleRequest
    ): ApiState<Unit> {
        return apiCall { scheduleApi.updateSchedule(scheduleId, request) }.map { Unit }
    }

    // 위치 변경하기
    override suspend fun updateScheduleLocation(
        scheduleId: Long,
        request: UpdateLocationRequest
    ): ApiState<UpdateLocationResponse> {
        return apiCall {
            scheduleApi.updateScheduleLocation(
                scheduleId,
                UpdateScheduleLocationV2Request(
                    location = ScheduleLocationV2Response(
                        latitude = request.latitude,
                        longitude = request.longitude,
                        locationName = request.locationName
                    )
                )
            )
        }.map {
            UpdateLocationResponse(scheduleId, request.locationName, request.latitude, request.longitude)
        }
    }


    override suspend fun createStudyGroupSchedule(
        request: CreateStudyGroupScheduleRequest
    ): ApiState<Long> {
        return apiCall { scheduleApi.createStudyGroupSchedule(request) }
    }

}
