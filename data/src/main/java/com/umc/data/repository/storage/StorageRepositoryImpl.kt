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
import com.umc.domain.repository.storage.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storageRemoteDatasource: StorageRemoteDataSource,
    @ApplicationContext private val context: Context
) : StorageRepository {
    override suspend fun uploadFile(uriString: String, category: String)
    : ApiState<UploadFileInfo> {
        val uri = Uri.parse(uriString)

        val meta = StorageUriUtil.getMetadata(context, uri)
            ?: return ApiState.Fail(FailState(code = "LOCAL_001", message = "파일 읽기 실패"))

        // 1. 파일 preupload (presigned url을 생성)
        val prepareState = storageRemoteDatasource.prepareUpload(
            PrepareUploadRequest(meta.name, meta.mimeType, meta.size, category)
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
}