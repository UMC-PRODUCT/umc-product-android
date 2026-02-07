package com.umc.domain.repository.schedule
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleDetailModel
import com.umc.domain.model.home.schedule.ScheduleListModel
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.model.act.check.UserCheckAvailable

interface ScheduleRepository {

    suspend fun getScheduleList(): ApiState<List<ScheduleListModel>>

    suspend fun getScheduleDetailHome(scheduleId: Int): ApiState<ScheduleDetailModel>

    suspend fun getMonthSchedule(year: Int, month: Int): ApiState<List<ScheduleMonthModel>>
  
    suspend fun getScheduleDetail(scheduleId: Int): ApiState<UserCheckAvailable>

   
}