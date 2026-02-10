package com.umc.domain.usecase.school

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetAllSchoolUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(): ApiState<List<SchoolInfo>> {
        return organizationRepository.getAllSchool()
    }
}