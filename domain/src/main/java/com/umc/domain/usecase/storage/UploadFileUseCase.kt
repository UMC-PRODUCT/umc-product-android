package com.umc.domain.usecase.storage

import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.repository.storage.StorageRepository
import javax.inject.Inject

/**
 * 파일의 Uri string하고, category를 전송하면, 파일 전송 API를 파이프라인으로 수행하여,
 * 수행 여부를 알려주는 usecase입니다.
 * domain과의 분리를 위해, Uri는 String으로 바꾸고, data에서 파싱합니다.
 * category의 경우, domain->enums->UploadFileCategory에 정의된 enum을 그대로 사용하시면 됩니다.
 *
 * 일단은 리턴형으로 fileId를 조회하도록 했습니다 (Swagger 개발용)
 * 현재 이미지의 경우에는 로드 후, 유저 정보 API로 url를 얻을 수 있습니다. 후에, 조정이 필요하면, UploadFileInfo의 인자 수정 및
 * StorageRepositoryImpl 수정 (TODO 표시)
 * **/

class UploadFileUseCase @Inject constructor(
    private val storageRepository: StorageRepository
){
    suspend operator fun invoke(uriString : String, category: UploadFileCategory) =
        storageRepository.uploadFile(uriString, category.name)
}