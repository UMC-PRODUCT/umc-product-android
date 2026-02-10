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

    override suspend fun prepareUpload(request: PrepareUploadRequest): ApiState<PrepareUploadResponse> {
        return apiCall { storageApi.prepareUpload(request) }
    }

    override suspend fun uploadFile(
        url: String,
        headers: Map<String, String>,
        body: RequestBody
    ): Response<Unit> {
        return storageApi.uploadFile(url, headers, body)
    }

    override suspend fun confirmUpload(fileId: String): ApiState<Unit> {
        return apiCall { storageApi.confirmUpload(fileId) }
    }


}