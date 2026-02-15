package com.umc.data.dataSource.remote.storage

import com.umc.data.api.StorageApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.request.storage.PrepareUploadRequest
import com.umc.data.response.storage.PrepareUploadResponse
import com.umc.domain.model.base.ApiState
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class StorageRemoteDataSourceImpl @Inject constructor(
    private val storageApi: StorageApi
) : StorageRemoteDataSource{

    //파일 업로드 준비
    override suspend fun prepareUpload(request: PrepareUploadRequest): ApiState<PrepareUploadResponse> {
        return apiCall { storageApi.prepareUpload(request) }
    }

    //파일 업로드 api
    override suspend fun uploadFile(
        url: String,
        headers: Map<String, String>,
        body: RequestBody
    ): Response<Unit> {
        return storageApi.uploadFile(url, headers, body)
    }

    //파일 업로드 확인
    override suspend fun confirmUpload(fileId: String): ApiState<Unit> {
        return apiCall { storageApi.confirmUpload(fileId) }
    }

    //파일 삭제
    override suspend fun deleteFile(fileId: String): ApiState<Unit> {
        return apiCall { storageApi.deleteFile(fileId) }
    }


}