package com.umc.presentation.notice

import com.umc.presentation.notice.write.MarkdownParser

/**
 * 공지 목록 카드 미리보기용 마크다운 평문 추출기.
 *
 * iOS 스펙 8절의 2-패스 서식 제거를 따라, 1차 패스에서 마크다운 마커와 이스케이프를 제거한 뒤
 * 2차 패스에서 이스케이프 해제 결과로 드러난 마커(`\*\*굿\*\*` → `**굿**`)까지 깔끔하게 정리한다.
 */
fun extractNoticePlainText(raw: String): String {
    if (raw.isBlank()) return ""
    val firstPass = MarkdownParser.plainText(raw)
    val secondPass = MarkdownParser.plainText(firstPass)
    return secondPass.trim()
}
