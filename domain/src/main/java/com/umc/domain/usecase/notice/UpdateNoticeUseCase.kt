package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeUpdateRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class UpdateNoticeUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(noticeId: Long, request: NoticeUpdateRequest): ApiState<Unit> {
        return noticeRepository.updateNotice(noticeId, request)
    }
}
