package com.umc.domain.model.notice

/**
 * 공지 작성 화면에 첨부된 이미지. 선택 즉시 업로드해 fileId를 확보한다.
 * domain과의 분리를 위해 Uri는 String으로 보관 (data/presentation에서 파싱)
 */
data class NoticeImageAttachment(
    val uri: String,
    val fileId: String,
)
