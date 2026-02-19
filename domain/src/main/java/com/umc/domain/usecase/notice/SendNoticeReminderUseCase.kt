package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeReminderRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class SendNoticeReminderUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(noticeId: Long, targetChallengerIds: List<Long>): ApiState<Unit> {
        val request = NoticeReminderRequest(targetIds = targetChallengerIds)
        return noticeRepository.sendNoticeReminder(noticeId, request)
    }
}
