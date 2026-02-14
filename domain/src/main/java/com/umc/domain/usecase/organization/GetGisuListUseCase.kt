package com.umc.domain.usecase.organization

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.GisuList
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class GetGisuListUseCase @Inject constructor(
    private val organizationRepository: OrganizationRepository
) {
    suspend operator fun invoke(): ApiState<GisuList> {
        return organizationRepository.getAllGisu()
    }
}