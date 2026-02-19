package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeVoteRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class AddNoticeVoteUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        noticeId: Long,
        request: NoticeVoteRequest
    ): ApiState<Unit> {
        return noticeRepository.addNoticeVote(noticeId, request)
    }
}
