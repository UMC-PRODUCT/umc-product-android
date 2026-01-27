package com.umc.domain.model.notice

import com.umc.domain.model.enums.NoticeCategory

data class NoticeDetail(
    val mustRead: Boolean = false,
    val category: NoticeCategory = NoticeCategory.CENTRAL_OFFICE,
    val title: String = "",
    val profileImage: String = "",
    val author: String = "",
    val date: String = "",
    val viewCount: Int = 0,
    val receiver: String = "",
    val content: String = "",
    val imageList: List<String> = emptyList(),
    val link: String = "",
    val vote: Vote = Vote(),
    val allReceiverCount: Int = 0,
    val nowReceiverCount: Int = 0,
    val receiverText: String = "",
    val userList: List<User> = emptyList()
)