package com.umc.domain.usecase

import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class GrantChallengerPointUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(
        id: Long,
        request: ChallengerPointRequest
    ): ApiState<ChallengerManageDialogModel> = repository.grantChallengerPoint(id, request)
}