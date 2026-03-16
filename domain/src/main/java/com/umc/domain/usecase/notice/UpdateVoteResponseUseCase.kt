package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.survey.VoteResponseRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class UpdateVoteResponseUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(voteId: Long, optionIds: List<Long>): ApiState<Unit> {
        val request = VoteResponseRequest(optionIds = optionIds)
        return noticeRepository.updateVoteResponse(voteId, request)
    }
}
