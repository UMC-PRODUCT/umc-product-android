package com.umc.domain.usecase.challenger

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class DeleteChallengerPointUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(challengerPointId: Long): ApiState<Unit> {
        return repository.deleteChallengerPoint(challengerPointId)
    }
}