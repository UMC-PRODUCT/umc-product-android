package com.umc.presentation.community.search

import com.umc.component.theme.AppStrings

internal fun validatedSearchOffset(raw: String?, currentOffset: Int): Int? {
    if (raw == null) return null
    return requireNotNull(raw.toIntOrNull()?.takeIf { it > currentOffset }) {
        AppStrings.COMMUNITY_SEARCH_CURSOR_ERROR
    }
}
