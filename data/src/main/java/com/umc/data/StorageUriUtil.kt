package com.umc.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.source

object StorageUriUtil {
    // Uri에서 메타데이터(이름, 타입, 크기) 추출
    fun getMetadata(context: Context, uri: Uri): UploadFileMetadata? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                UploadFileMetadata(name, mimeType, size)
            } else null
        }
    }

    // Uri를 서버 전송용 RequestBody로 변환
    /**참고. 최대 200MB의 파일을 보내기 위해 readStream 대신 okio.source() 사용**/
    fun toRequestBody(context: Context, uri: Uri): RequestBody {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        val size = getMetadata(context, uri)?.size ?: -1L

        return object : RequestBody() {
            override fun contentType() = mimeType?.toMediaTypeOrNull()
            override fun contentLength() = size
            override fun writeTo(sink: okio.BufferedSink) {
                contentResolver.openInputStream(uri)?.use { input ->
                    sink.writeAll(input.source()) //
                }
            }
        }
    }

    //파일 이름에서 타입 뽑아내기 (이결로 비교)
    fun getType(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    data class UploadFileMetadata(val name: String, val mimeType: String, val size: Long)

}