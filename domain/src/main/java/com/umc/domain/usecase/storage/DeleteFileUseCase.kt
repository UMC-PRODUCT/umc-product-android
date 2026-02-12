package com.umc.domain.usecase.storage

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.storage.StorageRepository
import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(
    private val storageRepository: StorageRepository
)  {
    suspend operator fun invoke(fileId: String): ApiState<Unit> {
        return storageRepository.deleteFile(fileId)
    }
}