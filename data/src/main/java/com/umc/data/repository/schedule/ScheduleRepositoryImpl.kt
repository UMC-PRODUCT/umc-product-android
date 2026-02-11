package com.umc.data.repository.schedule

import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleListResponse.Companion.toDomain
import com.umc.data.response.schedule.ScheduleMonthResponse.Companion.toDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.home.schedule.ScheduleListModel
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.data.response.schedule.ScheduleDetailResponse.Companion.toModel
import com.umc.data.response.schedule.ScheduleListResponse.Companion.toAdminDomain
import com.umc.data.response.schedule.ScheduleDetailResponse.Companion.toPlanDetailDomain
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource
) : ScheduleRepository {

    //일정 리스트 가져오기
    override suspend fun getScheduleList(): ApiState<List<ScheduleListModel>> {
        return scheduleRemoteDataSource.getScheduleList().map { responseList ->
            responseList.map { it.toDomain() }
        }
    }

    //일정 상세 정보 가져오기 (홈 화면 -> 일정 상세)
    override suspend fun getScheduleDetailHome(scheduleId: Long): ApiState<PlanDetailItem> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { it.toPlanDetailDomain() }
    }

    //일정 상세 정보 가져오기 (활동 탭)
    override suspend fun getScheduleDetail(scheduleId: Long): ApiState<UserCheckAvailable> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId.toLong()).map { response ->
            response.toModel()
        }
    }

    //월별 일정 조회
    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthModel>> {
        return scheduleRemoteDataSource.getMonthSchedule(year, month).map { responseList ->
            responseList.map { it.toDomain() }
        }
    }

    //관리자 일정 리스트 가져오기
    override suspend fun getAdminScheduleList(): ApiState<List<AdminSessionCheck>> {
        return scheduleRemoteDataSource.getScheduleList().map { responseList ->
            responseList.map { it.toAdminDomain() }
        }
    }

    //일정 생성하기
    override suspend fun createSchedule(request: CreateSchedule): ApiState<Long> {
        val request = CreateScheduleRequest(
            name = request.name,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            isAllDay = request.isAllDay,
            locationName = request.locationName,
            latitude = request.latitude,
            longitude = request.longitude,
            description = request.description,
            participantMemberIds = request.participantMemberIds,
            tags = request.tags,
            gisuId = request.gisuId,
            requiresApproval = request.requiresApproval
        )
        return scheduleRemoteDataSource.createScheduleWithAttendance(request).map {
            it.toLong()
        }
    }

    //일정 수정하기
    override suspend fun updateSchedule(scheduleId: Long, request: UpdateSchedule): ApiState<Unit> {
        val request = UpdateScheduleRequest(
            name = request.name,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            isAllDay = request.isAllDay,
            locationName = request.locationName,
            latitude = request.latitude,
            longitude = request.longitude,
            description = request.description,
            tags = request.tags
        )
        return scheduleRemoteDataSource.updateSchedule(scheduleId, request)
    }

    //일정 삭제하기
    override suspend fun deleteSchedule(scheduleId: Long): ApiState<Unit> {
        return scheduleRemoteDataSource.deleteScheduleWithAttendance(scheduleId)
    }

    //

}