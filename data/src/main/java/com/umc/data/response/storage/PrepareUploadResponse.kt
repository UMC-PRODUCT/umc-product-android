package com.umc.data.response.storage

data class PrepareUploadResponse(
    val fileId: String,
    val uploadUrl: String,
    val uploadMethod: String,
    val headers: Map<String, String>,
    val expiresAt: String
)
