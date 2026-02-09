package com.umc.domain.usecase.schedule

import com.umc.domain.repository.schedule.ScheduleRepository
import javax.inject.Inject

/**위 UseCase는 GetSchduleDetailUseCase와 달리, 홈->일정 상세 페이지에서 쓰는 USeCse다.**/
class GetScheduleDetailHomeUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: Long) = scheduleRepository.getScheduleDetailHome(scheduleId)
}