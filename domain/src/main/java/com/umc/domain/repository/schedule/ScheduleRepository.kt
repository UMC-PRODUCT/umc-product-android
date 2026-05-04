package com.umc.domain.repository.schedule
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleCapabilities
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.home.PlanDetailItem
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
}