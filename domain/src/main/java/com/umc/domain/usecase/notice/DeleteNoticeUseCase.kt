package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class DeleteNoticeUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(noticeId: Long): ApiState<Unit> {
        return noticeRepository.deleteNotice(noticeId)
    }
}
