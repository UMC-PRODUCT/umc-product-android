package com.umc.domain.repository.storage

import com.umc.domain.model.UploadFileInfo
import com.umc.domain.model.base.ApiState

interface StorageRepository {
    //파일 업로드 API (파이프라인으로 파일 정보 전송 -> url get -> 바로 올리고 최종 완료 로직)
    suspend fun uploadFile(
        uriString: String,
        category: String
    ): ApiState<UploadFileInfo>


}