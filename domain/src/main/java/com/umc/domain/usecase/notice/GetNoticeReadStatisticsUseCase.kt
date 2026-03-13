package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class GetNoticeReadStatisticsUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(noticeId: Long): ApiState<NoticeReadStatistics> {
        return noticeRepository.getNoticeReadStatistics(noticeId)
    }
}
