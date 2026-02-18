package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class GetNoticeDetailUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(noticeId: Long): ApiState<NoticeDetail> {
        return noticeRepository.getNoticeDetail(noticeId)
    }
}
