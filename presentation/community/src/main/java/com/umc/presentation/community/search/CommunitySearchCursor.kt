package com.umc.presentation.community.search

internal fun validatedSearchOffset(raw: String?, currentOffset: Int): Int? {
    if (raw == null) return null
    return requireNotNull(raw.toIntOrNull()?.takeIf { it > currentOffset }) {
        "검색 페이지 정보가 올바르지 않아요. 다시 시도해주세요."
    }
}
