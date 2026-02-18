package com.umc.domain.model.base

data class CursorPage<T>(
    val content: List<T>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)
