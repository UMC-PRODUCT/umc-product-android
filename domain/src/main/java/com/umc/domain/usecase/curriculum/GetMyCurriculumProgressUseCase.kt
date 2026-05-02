package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.study.StudyProgress
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetMyCurriculumProgressUseCase @Inject constructor(
    private val repository: CurriculumRepository
) {
    suspend operator fun invoke(
        gisuId: Long = 3L,
        part: String = "SPRINGBOOT" // 일단 하드코딩, 나중에 로그인 정보에서 가져오기
    ): ApiState<StudyProgress> {
        return repository.getCurriculumOverview(gisuId, part)
    }
}
