package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class GetAdminChallengerDetailUseCase @Inject constructor(
    private val challengerRepository: ChallengerRepository
) {
    suspend operator fun invoke(id: Long): ApiState<ChallengerManageDialogModel> {
        return challengerRepository.getAdminChallengerInfo(id)
    }
}