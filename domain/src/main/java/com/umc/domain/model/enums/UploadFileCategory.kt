package com.umc.domain.model.enums


/**파일 카테고리별 업로드 크기 제한을 명시
 * repository에서 거르는 용도로 사용
 * **/
enum class UploadFileCategory(val label: String,
                              val maxSizeBytes: Long,
                              val alloweType: List<String>) {
    PROFILE_IMAGE("프로필 이미지", 5 * 1024 * 1024,
        listOf("jpg", "jpeg", "png", "webp")),
    POST_IMAGE("게시글 이미지", 10 * 1024 * 1024,
        listOf("jpg", "jpeg", "png", "webp", "gif")),
    POST_ATTACHMENT("게시글 첨부파일", 50 * 1024 * 1024,
        listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip")),
    NOTICE_ATTACHMENT("공지사항 첨부파일", 10 * 1024 * 1024,
        listOf("jpg", "jpeg", "png", "webp", "gif")),
    WORKBOOK_SUBMISSION("워크북 제출 파일", 20 * 1024 * 1024,
        listOf("pdf", "jpg", "jpeg", "png", "zip")),
    SCHOOL_LOGO("학교 로고 이미지", 5 * 1024 * 1024,
        listOf("jpg", "jpeg", "png", "webp")),
    PORTFOLIO("포트폴리오", 200 * 1024 * 1024,
        listOf("pdf")),
    ETC("기타", 10 * 1024 * 1024,
        emptyList());


}