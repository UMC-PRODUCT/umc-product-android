package com.umc.data.request.storage

data class PrepareUploadRequest(
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val category: String
)