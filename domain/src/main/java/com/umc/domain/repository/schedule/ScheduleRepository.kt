package com.umc.domain.repository.schedule
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleCapabilities
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.model.act.check.AttendanceDecision
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule

interface ScheduleRepository {

    //일정 생성·수정 권한 조회
    suspend fun getScheduleCapabilities(): ApiState<ScheduleCapabilities>

    //일정 상세 정보 가져오기 (홈 화면 -> 일정 상세)
    suspend fun getScheduleDetailHome(scheduleId: Long): ApiState<PlanDetailItem>

    //월별 일정 가져오기
    suspend fun getMonthSchedule(year: Int, month: Int): ApiState<List<ScheduleMonthModel>>

    // 출석 가능 세션 조회 (isAttendanceRequired=true) / 이력 조회 (isAttendanceRequired=false)
    suspend fun getAttendanceAvailableSessions(isAttendanceRequired: Boolean): ApiState<List<UserCheckAvailable>>

  
    suspend fun getScheduleDetail(scheduleId: Long): ApiState<UserCheckAvailable>

    //운영진 일정 리스트 가져오기
    suspend fun getAdminScheduleList(): ApiState<List<AdminSessionCheck>>

    //일정 생성하기
    suspend fun createSchedule(request: CreateSchedule) : ApiState<Long>

    //일정 수정하기
    suspend fun updateSchedule(scheduleId: Long, request: UpdateSchedule) : ApiState<Unit>

    //일정 삭제하기
    suspend fun deleteSchedule(scheduleId: Long) : ApiState<Unit>

    // 위치 변경하기
    suspend fun updateScheduleLocation(
        scheduleId: Long,
        locationName: String,
        latitude: Double,
        longitude: Double
    ): ApiState<Unit>

    // 스터디 그룹 일정 생성
    suspend fun createStudyGroupSchedule(request: CreateStudyGroupSchedule): ApiState<Long>

    // 일정 출석 요청
    suspend fun postAttendanceRequest(scheduleId: Long, locationVerified: Boolean, latitude: Double?, longitude: Double?): ApiState<Unit>

    // 출석 요청 승인/거절
    suspend fun postAttendanceDecide(scheduleId: Long, decisions: List<AttendanceDecision>): ApiState<Unit>

    // 사유 출석 제출
    suspend fun postAttendanceExcuse(scheduleId: Long, excuseReason: String, isVerified: Boolean, latitude: Double?, longitude: Double?): ApiState<Unit>

    // 출석 이력 조회 (admin용)
    suspend fun getAttendanceHistory(from: String? = null, to: String? = null, attendanceStatus: String? = null): ApiState<List<AdminSessionCheck>>

    // 단일 일정 승인 대기 유저 조회
    suspend fun getPendingUsers(scheduleId: Long): ApiState<List<AdminPendingUser>>
}