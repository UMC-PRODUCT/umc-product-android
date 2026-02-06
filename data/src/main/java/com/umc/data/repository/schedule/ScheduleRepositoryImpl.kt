package com.umc.data.repository.schedule

import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.response.schedule.ScheduleDetailResponse.Companion.toHomeDomain
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

    override suspend fun getScheduleDetailHome(scheduleId: Int): ApiState<ScheduleDetailModel> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { it.toHomeDomain() }
    }

    /*
    override suspend fun getScheduleDetail(scheduleId: Int): ApiState<UserCheckAvailable> {
        return scheduleRemoteDataSource.getScheduleDetail(scheduleId).map { response ->
            response.toModel()
        }
    }

     */



    override suspend fun getMonthSchedule(
        year: Int,
        month: Int
    ): ApiState<List<ScheduleMonthModel>> {
        return scheduleRemoteDataSource.getMonthSchedule(year, month).map { responseList ->
            responseList.map { it.toDomain() }
        }
    }

}