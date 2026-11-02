package com.umc.presentation.community.chatting

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.umc.domain.model.enums.UploadFileCategory
import java.io.InputStream

internal const val COMMUNITY_CHAT_MAX_TOTAL_IMAGE_BYTES = 40L * 1024 * 1024

internal enum class CommunityChatImageSizeError { IMAGE_TOO_LARGE, TOTAL_TOO_LARGE, UNREADABLE }

internal fun validateCommunityChatImageSizes(sizes: List<Long?>): CommunityChatImageSizeError? {
    if (sizes.any { it == null || it < 0 }) return CommunityChatImageSizeError.UNREADABLE
    var total = 0L
    for (size in sizes.filterNotNull()) {
        if (size > COMMUNITY_CHAT_MAX_TOTAL_IMAGE_BYTES - total) return CommunityChatImageSizeError.TOTAL_TOO_LARGE
        total += size
    }
    return if (sizes.filterNotNull().any { it > UploadFileCategory.ETC.maxSizeBytes }) {
        CommunityChatImageSizeError.IMAGE_TOO_LARGE
    } else null
}

internal fun communityChatImageSize(resolver: ContentResolver, uri: Uri): Long? {
    val size = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val column = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && column >= 0 && !cursor.isNull(column)) cursor.getLong(column) else null
        }
    }.getOrNull()?.takeIf { it >= 0 }
    return measureCommunityChatImageSize(size) { resolver.openInputStream(uri) }
}

internal fun measureCommunityChatImageSize(
    reportedSize: Long?,
    limit: Long = UploadFileCategory.ETC.maxSizeBytes,
    openStream: () -> InputStream?,
): Long? {
    if (reportedSize != null && reportedSize > limit) return reportedSize
    // 한도 이하라는 메타데이터도 신뢰하지 않고 실제 스트림을 한도+1까지만 확인한다.
    return runCatching {
        openStream()?.use { readCommunityChatImageSize(it, limit) }
    }.getOrNull()
}

internal fun readCommunityChatImageSize(
    input: InputStream,
    limit: Long = UploadFileCategory.ETC.maxSizeBytes,
): Long {
    val buffer = ByteArray(8192)
    var total = 0L
    while (total <= limit) {
        val count = input.read(buffer, 0, minOf(buffer.size.toLong(), limit - total + 1).toInt())
        if (count < 0) return total
        total += count
    }
    return total
}
