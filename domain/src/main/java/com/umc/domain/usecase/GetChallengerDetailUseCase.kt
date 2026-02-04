package com.umc.domain.usecase

import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class GetChallengerDetailUseCase @Inject constructor(
    private val challengerRepository: ChallengerRepository
) {
    suspend operator fun invoke(id: Long): ApiState<ChallengerInfoDialogModel> {
        return challengerRepository.getChallengerInfo(id)
    }
}