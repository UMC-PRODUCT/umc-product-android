package com.umc.domain.model.notice

data class NoticeChipState(
    val text: String,
    val isClicked: Boolean = false,
    val hanBottomSheet: Boolean = false,
)
