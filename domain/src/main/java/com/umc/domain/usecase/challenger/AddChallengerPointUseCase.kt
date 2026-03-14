package com.umc.domain.usecase.challenger

import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class AddChallengerPointUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(challengerId: Long,
                                pointType: String,
                                pointValue: Int,
                                description: String): ApiState<UserInfo> {

        return repository.addChallengerPoint(challengerId, pointType, pointValue, description)
    }
}