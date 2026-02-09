package com.umc.data.api

import com.umc.data.request.storage.PrepareUploadRequest
import com.umc.data.response.storage.PrepareUploadResponse
import com.umc.domain.model.base.ApiResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

interface StorageApi {

    //1. 파일 업로드 준비
    @POST(Endpoints.Storage.PRE_UPLOAD)
    suspend fun prepareUpload(
        @Body request: PrepareUploadRequest
    ): ApiResponse<PrepareUploadResponse>

    //2. response로 받은 URL을 직접 업로드 하기
    @PUT
    suspend fun uploadFile(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body file: RequestBody
    ): Response<Unit>

    //3. 업로드가 완료되었으면 확정받기
    @POST(Endpoints.Storage.CONFIRM_UPLOAD)
    suspend fun confirmUpload(
        @Path("fileId") fileId: String
    ) : ApiResponse<Unit>


    // 파일 삭제
    @DELETE(Endpoints.Storage.FILE_DELETE)
    suspend fun deleteFile(
        @Path("fileId") fileId: String
    ) : ApiResponse<Unit>
}