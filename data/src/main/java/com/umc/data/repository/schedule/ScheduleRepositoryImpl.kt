package com.umc.data.repository.schedule

import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.request.schedule.CreateScheduleRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.data.request.schedule.UpdateScheduleRequest
import com.umc.data.response.schedule.ScheduleCapabilitiesResponse.Companion.toDomain
import com.umc.data.response.schedule.ScheduleMeResponse.Companion.toAdminDomain
import com.umc.data.response.schedule.ScheduleMeResponse.Companion.toModel
import com.umc.data.response.schedule.ScheduleMeResponse.Companion.toMonthDomain
import com.umc.data.response.schedule.ScheduleMeResponse.Companion.toPlanDetailDomain
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.home.schedule.ScheduleCapabilities
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.data.request.schedule.ScheduleAttendanceRequest
import com.umc.domain.model.request.schedule.UpdateLocationRequest
import com.umc.domain.repository.schedule.ScheduleRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource
) : ScheduleRepository {

    private val utcIsoFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .withZone(ZoneOffset.UTC)

    //일정 생성·수정 권한 조회
    override suspend fun getScheduleCapabilities(): ApiState<ScheduleCapabilities> {
        return scheduleRemoteDataSource.getScheduleCapabilities().map { it.toDomain() }
    }

    //일정 상세 정보 가져오기 (홈 화면 -> 일정 상세)
    override suspend fun getScheduleDetailHome(scheduleId: Long): ApiState<PlanDetailItem> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { it.toPlanDetailDomain() }
    }

    //일정 상세 정보 가져오기 (활동 탭)
    override suspend fun getScheduleDetail(scheduleId: Long): ApiState<UserCheckAvailable> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { response ->
            response.toModel()
        }
    }

    //월별 일정 조회 - 로컬 시간대 기준 해당 월의 시작/끝을 UTC로 변환해 조회
    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthModel>> {
        val (from, to) = monthToUtcRange(year, month)
        return scheduleRemoteDataSource.getSchedules(from, to, isAttendanceRequired = false).map { list ->
            list.map { it.toMonthDomain() }
        }
    }

    //운영진 일정 리스트 가져오기 - 출석 필요 일정, 현재 기준 ±6개월 범위
    override suspend fun getAdminScheduleList(): ApiState<List<AdminSessionCheck>> {
        val (from, to) = adminScheduleUtcRange()
        return scheduleRemoteDataSource.getSchedules(from, to, isAttendanceRequired = true).map { list ->
            list.map { it.toAdminDomain() }
        }
    }

    //일정 생성하기
    override suspend fun createSchedule(request: CreateSchedule): ApiState<Long> {
        val req = CreateScheduleRequest(
            name = request.name,
            description = request.description,
            tags = request.tags,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            location = request.location?.let {
                CreateScheduleRequest.LocationRequest(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    locationName = it.locationName
                )
            },
            attendancePolicy = request.attendancePolicy?.let {
                CreateScheduleRequest.AttendancePolicyRequest(
                    checkInStartAt = it.checkInStartAt,
                    onTimeEndAt = it.onTimeEndAt,
                    lateEndAt = it.lateEndAt
                )
            },
            participantMemberIds = request.participantMemberIds
        )
        return scheduleRemoteDataSource.createSchedule(req)
    }

    //일정 수정하기
    override suspend fun updateSchedule(scheduleId: Long, request: UpdateSchedule): ApiState<Unit> {
        val req = UpdateScheduleRequest(
            name = request.name,
            description = request.description,
            tags = request.tags,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            location = request.location?.let {
                UpdateScheduleRequest.LocationRequest(it.latitude, it.longitude, it.locationName)
            },
            isOnline = request.isOnline,
            isAttendanceRequired = request.isAttendanceRequired,
            attendancePolicy = request.attendancePolicy?.let {
                UpdateScheduleRequest.AttendancePolicyRequest(it.checkInStartAt, it.onTimeEndAt, it.lateEndAt)
            },
            participantMemberIds = request.participantMemberIds
        )
        return scheduleRemoteDataSource.updateSchedule(scheduleId, req)
    }

    //일정 삭제하기
    override suspend fun deleteSchedule(scheduleId: Long): ApiState<Unit> {
        return scheduleRemoteDataSource.deleteScheduleWithAttendance(scheduleId)
    }

    override suspend fun updateScheduleLocation(
        scheduleId: Long,
        locationName: String,
        latitude: Double,
        longitude: Double
    ): ApiState<Unit> {
        val request = UpdateLocationRequest(locationName, latitude, longitude)
        return scheduleRemoteDataSource.updateScheduleLocation(scheduleId, request).map {}
    }

    override suspend fun postAttendanceRequest(
        scheduleId: Long,
        locationVerified: Boolean,
        latitude: Double?,
        longitude: Double?
    ): ApiState<Unit> {
        val request = ScheduleAttendanceRequest(locationVerified, latitude, longitude)
        return scheduleRemoteDataSource.postAttendanceRequest(scheduleId, request)
    }

    override suspend fun createStudyGroupSchedule(request: CreateStudyGroupSchedule): ApiState<Long> {
        val req = CreateStudyGroupScheduleRequest(
            name = request.name,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            isAllDay = request.isAllDay,
            locationName = request.locationName,
            latitude = request.latitude,
            longitude = request.longitude,
            description = request.description,
            tags = request.tags,
            studyGroupId = request.studyGroupId,
            gisuId = request.gisuId,
            requiresApproval = request.requiresApproval
        )
        return scheduleRemoteDataSource.createStudyGroupSchedule(req)
    }

    // 로컬 시간대 기준 해당 월의 시작(00:00:00.000) ~ 끝(23:59:59.999)을 UTC ISO8601로 변환
    private fun monthToUtcRange(year: Int, month: Int): Pair<String, String> {
        val yearMonth = YearMonth.of(year, month)
        val from: ZonedDateTime = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
        val to: ZonedDateTime = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59, 999_000_000)
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
        return utcIsoFormatter.format(from) to utcIsoFormatter.format(to)
    }

    // 현재 기준 ±6개월 범위를 UTC ISO8601로 변환
    private fun adminScheduleUtcRange(): Pair<String, String> {
        val today = LocalDate.now()
        val from: ZonedDateTime = today.minusMonths(6)
            .atStartOfDay(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
        val to: ZonedDateTime = today.plusMonths(6)
            .atTime(23, 59, 59, 999_000_000)
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
        return utcIsoFormatter.format(from) to utcIsoFormatter.format(to)
    }
}
