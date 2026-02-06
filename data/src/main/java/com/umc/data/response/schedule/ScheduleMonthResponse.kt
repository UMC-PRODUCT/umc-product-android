package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.home.schedule.ScheduleMonthModel

//월별 일정 조회
data class ScheduleMonthResponse(
    @SerializedName("scheduleId") val scheduleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("dDay") val dDay: String
) {
    companion object {
        fun ScheduleMonthResponse.toDomain(): ScheduleMonthModel {
            // "T"를 기준으로 날짜와 시간을 분리
            fun String.parseDateTime(): Pair<String, String> {
                val dateTimeParts = this.split("T")
                val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
                val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""
                return Pair(date, time)
            }

            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()


            return ScheduleMonthModel(
                scheduleId = scheduleId.toIntOrNull() ?: 0,
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