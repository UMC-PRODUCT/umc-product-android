package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class GetAdminChallengerListUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        keyword: String? = null,
        part: String? = null
    ): ApiState<AdminChallengerList> {
        return repository.getAdminChallengerList(cursor, size, schoolId, gisuId, keyword, part)
    }
}