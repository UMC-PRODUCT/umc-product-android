package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeCreateRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class CreateNoticeUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(request: NoticeCreateRequest): ApiState<Long> {
        return noticeRepository.createNotice(request)
    }
}
