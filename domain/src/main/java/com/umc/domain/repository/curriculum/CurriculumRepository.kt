package com.umc.domain.repository.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.study.StudyProgress

interface CurriculumRepository {
    suspend fun getMyCurriculumProgress(page: Int?, limit: Int?): ApiState<StudyProgress>

    suspend fun submitChallengerWorkbook(challengerWorkbookId: Long, submission: String): ApiState<Unit>
}
