package com.umc.domain.usecase.schedule

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.repository.schedule.ScheduleRepository
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetScheduleMonthUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(
        year: Int,
        month: Int,
        isAttendanceRequired: Boolean = false
    ): ApiState<List<ScheduleMonthModel>> {
        val yearMonth = YearMonth.of(year, month)

        // 해당 월의 1일 00:00:00Z
        val fromZdt = yearMonth.atDay(1).atStartOfDay().atZone(ZoneOffset.UTC)
        // 해당 월의 마지막 날 23:59:59Z
        val toZdt = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val fromStr = fromZdt.format(formatter)
        val toStr = toZdt.format(formatter)

        return scheduleRepository.getMySchedule(fromStr, toStr, isAttendanceRequired)
    }

}