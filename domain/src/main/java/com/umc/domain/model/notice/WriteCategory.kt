package com.umc.domain.model.notice

import com.umc.domain.model.enums.WriteCategoryType

/** 공지 작성 카테고리 (종류 + 표시 라벨) */
data class WriteCategory(
    val type: WriteCategoryType,
    val label: String,
)
