package com.umc.domain.model.notice

data class NoticeChipState(
    val text: String,
    val isClicked: Boolean = false,
    val schoolId: Long? = null,
    val chapterId: Long? = null,
    val part: String? = null,
    val hanBottomSheet: Boolean = false,
    val gisuId: Long? = null
)