package com.umc.data.repository.storage

import android.content.Context
import android.net.Uri
import com.umc.data.StorageUriUtil
import com.umc.data.dataSource.remote.storage.StorageRemoteDataSource
import com.umc.data.request.storage.PrepareUploadRequest
import com.umc.domain.model.UploadFileInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.map
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.repository.storage.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storageRemoteDatasource: StorageRemoteDataSource,
    @ApplicationContext private val context: Context
) : StorageRepository {
    override suspend fun uploadFile(uriString: String, category: UploadFileCategory)
    : ApiState<UploadFileInfo> {
        val uri = Uri.parse(uriString)

        val meta = StorageUriUtil.getMetadata(context, uri)
            ?: return ApiState.Fail(FailState(code = "LOCAL_001", message = "파일 읽기 실패"))

        // 추가!!! 여기서 카테고리별 파일 크기를 비교해서 reject 로직을 수행
        if (meta.size > category.maxSizeBytes) {
            val maxSizeMb = category.maxSizeBytes / (1024 * 1024)
            return ApiState.Fail(
                FailState(
                    code = "FILE_SIZE_EXCEEDED",
                    message = "${category.label}는 최대 ${maxSizeMb}MB까지 업로드 가능합니다."
                )
            )
        }

        // 여기서는 카테고리별 확장자 타입이 맞는지 비교해서 reject 로직을 수행
//        val type = StorageUriUtil.getType(meta.name)
//        if (category.alloweType.isNotEmpty() && !category.alloweType.contains(type)) {
//            return ApiState.Fail(
//                FailState(
//                    code = "INVALID_FILE_TYPE",
//                    message = "${category.label}에 허용되지 않는 파일 형식(.${type})입니다."
//                )
//            )
//        }
        
        // 1. 파일 preupload (presigned url을 생성)
        val prepareState = storageRemoteDatasource.prepareUpload(
            PrepareUploadRequest(meta.name, meta.mimeType, meta.size, category.name)
        )

        // 만약 결과값인 prepareState가 성공이면 S3 업로드를 수행한다.
        return when (prepareState) {
            is ApiState.Success -> {
                val uploadData = prepareState.data

                // 2. presigned url을 이용해 직접 전송한다.
                val requestBody = StorageUriUtil.toRequestBody(context, uri)
                val uploadResponse = storageRemoteDatasource.uploadFile(
                    uploadData.uploadUrl, uploadData.headers, requestBody
                )

                // 얘는 ApiState가 아니라 직접 response로 감쌌기 때문에 isSuccessful로 비교
                if (uploadResponse.isSuccessful) {
                    // 3. Confirm 즉, 파일 업로드 완료를 수행한다.
                    val confirmState = storageRemoteDatasource.confirmUpload(uploadData.fileId)
                    return when (confirmState) {
                        is ApiState.Success -> {
                            // 성공했다면 서버가 준 Unit 결과는 무시하고, 우리가 필요한 UploadFileInfo를 반환
                            ApiState.Success(UploadFileInfo(fileId = uploadData.fileId))
                        }
                        is ApiState.Fail -> {
                            ApiState.Fail(confirmState.failState)
                        }
                    }
                } else {
                    ApiState.Fail(FailState(code = "S3_001", message = "S3 전송 실패"))
                }
            }
            is ApiState.Fail -> prepareState
        }
    }


    //파일 삭제 API
    override suspend fun deleteFile(fileId: String): ApiState<Unit> {
        return storageRemoteDatasource.deleteFile(fileId)
    }

}