package com.umc.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.source

object StorageUriUtil {
    // Uri에서 메타데이터(이름, 타입, 크기) 추출.
    // ContentResolver 조회는 Binder·디스크 I/O 이므로 호출부가 IO 디스패처에서 부른다.
    fun getMetadata(context: Context, uri: Uri): UploadFileMetadata? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            // 제공자가 이름·크기 컬럼을 주지 않으면 인덱스가 -1 이라 getString 이 예외를 던진다. 읽지 못한 파일로 본다
            if (nameIndex < 0 || sizeIndex < 0) return@use null
            if (cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex) ?: return@use null
                val size = cursor.getLong(sizeIndex)
                UploadFileMetadata(name, mimeType, size)
            } else null
        }
    }

    // Uri를 서버 전송용 RequestBody로 변환
    // 타입·크기는 getMetadata 에서 이미 구한 값을 받아 ContentResolver 를 다시 조회하지 않는다.
    /**참고. 최대 200MB의 파일을 보내기 위해 readStream 대신 okio.source() 사용**/
    fun toRequestBody(context: Context, uri: Uri, meta: UploadFileMetadata): RequestBody {
        val contentResolver = context.contentResolver
        val mediaType = meta.mimeType.toMediaTypeOrNull()

        return object : RequestBody() {
            override fun contentType() = mediaType
            override fun contentLength() = meta.size
            // 실제 스트림 읽기는 OkHttp 가 요청을 보내는 스레드에서 일어난다.
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
