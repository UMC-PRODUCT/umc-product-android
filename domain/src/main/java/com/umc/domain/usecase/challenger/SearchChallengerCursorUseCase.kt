package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.ChallengerList
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class SearchChallengerCursorUseCase @Inject constructor(
    private val repository: ChallengerRepository
) {
    suspend operator fun invoke(
        cursor: Long?,
        size: Int,
        schoolId: Long?,
        gisuId: Long?,
        part: String?,
        name: String?,
        nickname: String?
    ): ApiState<ChallengerList> {
        return repository.searchChallengerCursor(cursor, size, schoolId, gisuId, part, name, nickname)
    }
}