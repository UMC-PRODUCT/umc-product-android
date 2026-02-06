package com.umc.data.repository.schedule

import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.response.schedule.ScheduleDetailResponse.Companion.toDomain
import com.umc.data.response.schedule.ScheduleListResponse.Companion.toDomain
import com.umc.data.response.schedule.ScheduleMonthResponse.Companion.toDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.home.schedule.ScheduleDetailModel
import com.umc.domain.model.home.schedule.ScheduleListModel
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource
) : ScheduleRepository {

    override suspend fun getScheduleList(): ApiState<List<ScheduleListModel>> {
        return scheduleRemoteDataSource.getScheduleList().map { responseList ->
            responseList.map { it.toDomain() }
        }
    }

    override suspend fun getScheduleDetail(scheduleId: Int): ApiState<ScheduleDetailModel> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { it.toDomain() }
    }

    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthModel>> {
        return scheduleRemoteDataSource.getMonthSchedule(year, month).map { responseList ->
            responseList.map { it.toDomain() }
        }
    }

}