package com.umc.data.dataSource.remote.storage

import com.umc.data.request.storage.PrepareUploadRequest
import com.umc.data.response.storage.PrepareUploadResponse
import com.umc.domain.model.base.ApiState
import okhttp3.RequestBody
import retrofit2.Response

interface StorageRemoteDataSource {
    //파일 업로드 준비 (url 받음)
    suspend fun prepareUpload(request: PrepareUploadRequest):
            ApiState<PrepareUploadResponse>

    //직접 S3에 저장
    suspend fun uploadFile(url: String, headers: Map<String, String>, body: RequestBody):
            Response<Unit>

    //파일 업로드 확인
    suspend fun confirmUpload(fileId: String): ApiState<Unit>

    //파일 삭제
    suspend fun deleteFile(fileId: String): ApiState<Unit>

}