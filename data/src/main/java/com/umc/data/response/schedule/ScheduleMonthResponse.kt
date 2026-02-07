package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.home.schedule.ScheduleMonthModel

//월별 일정 조회
data class ScheduleMonthResponse(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("dDay") val dDay: Int
) {
    companion object {
        fun ScheduleMonthResponse.toDomain(): ScheduleMonthModel {

            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()


            return ScheduleMonthModel(
                scheduleId = scheduleId,
                name = name,
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                status = status,
                dDay = dDay
            )
        }
    }
}