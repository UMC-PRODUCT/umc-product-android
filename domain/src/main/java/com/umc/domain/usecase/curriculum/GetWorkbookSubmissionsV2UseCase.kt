package com.umc.domain.usecase.curriculum

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.curriculum.StudyMemberSubmissionPage
import com.umc.domain.repository.curriculum.CurriculumRepository
import javax.inject.Inject

class GetWorkbookSubmissionsV2UseCase @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
) {
    suspend operator fun invoke(
        studyGroupId: Long? = null,
        weekNos: List<Long>? = null,
        cursor: Long? = null,
        size: Int = 20,
    ): ApiState<StudyMemberSubmissionPage> {
        return curriculumRepository.getWorkbookSubmissionsV2(
            studyGroupId = studyGroupId,
            weekNos = weekNos,
            cursor = cursor,
            size = size,
        )
    }
}