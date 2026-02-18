package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class GetChallengerListUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?
    ): ApiState<ChallengerList> = repository.getChallengerList(cursor, size, schoolId, gisuId)
}
