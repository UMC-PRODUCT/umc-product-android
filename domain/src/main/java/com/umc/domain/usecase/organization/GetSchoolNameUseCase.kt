package com.umc.domain.usecase.organization

import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetSchoolNameUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
)  {
    suspend operator fun invoke(schoolId: Long) =
        organizationRepository.getSchoolDetail(schoolId)
}
