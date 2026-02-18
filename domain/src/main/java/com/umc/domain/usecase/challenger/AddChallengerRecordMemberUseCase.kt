package com.umc.domain.usecase.challenger

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class AddChallengerRecordMemberUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(
        request: ChallengerRecordMemberRequest
    ): ApiState<Unit> = repository.addChallengerRecordMember(request)
}
