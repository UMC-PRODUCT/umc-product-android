package com.umc.presentation.ui.notice.write.model

import android.net.Uri
data class NoticeImageItem(
    val id: Long = 0L,  // 기존 이미지 ID 또는 업로드 후 받은 ID
    val uri: Uri? = null,  // 새로 선택한 이미지 URI
    val url: String = ""  // 기존 이미지 URL (표시용)
) {
    fun getDisplaySource(): Any {
        return uri ?: url
    }
    fun isNewImage(): Boolean {
        return id == 0L && uri != null
    }

    fun isExistingImage(): Boolean {
        return id != 0L && url.isNotBlank()
    }
}
