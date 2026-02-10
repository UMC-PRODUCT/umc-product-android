package com.umc.domain.repository.schedule
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleListModel
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.home.PlanDetailItem

interface ScheduleRepository {

    suspend fun getScheduleList(): ApiState<List<ScheduleListModel>>

    suspend fun getScheduleDetailHome(scheduleId: Long): ApiState<PlanDetailItem>

    suspend fun getMonthSchedule(year: Int, month: Int): ApiState<List<ScheduleMonthModel>>
  
    suspend fun getScheduleDetail(scheduleId: Long): ApiState<UserCheckAvailable>

    suspend fun getAdminScheduleList(): ApiState<List<AdminSessionCheck>>
}