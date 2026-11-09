package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
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
        part: String? = null,
        chapterId: Long? = null,
    ): ApiState<AdminChallengerList> {
        // null은 서버에서 활성 기수로 해석되므로, 선택 기수가 없을 때는 검색하지 않는다.
        if (gisuId == null || gisuId <= 0) {
            return ApiState.Fail(FailState(code = "GISU_REQUIRED", message = "조회할 기수를 선택해 주세요."))
        }
        return repository.getAdminChallengerList(cursor, size, schoolId, gisuId, keyword, part, chapterId)
    }
}
