package com.umc.domain.usecase

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.GisuInfo
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetGisuInfoUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(gisuId: Long): ApiState<GisuInfo>
    = organizationRepository.getGisuInfo(gisuId)
}