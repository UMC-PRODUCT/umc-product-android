package com.umc.component.util

// 웹 URL 형식 검증 정규식.
// 매 호출마다 컴파일되지 않도록 파일 최상단에서 한 번만 생성
// http/https 로 시작하고, 점이 있는 호스트(+ 선택적 포트·경로·쿼리·해시)를 요구한다
private val WEB_URL_REGEX = Regex(
    pattern = "https?://[^\\s/?#.]+(\\.[^\\s/?#.]+)+(:\\d+)?([/?#]\\S*)?",
    option = RegexOption.IGNORE_CASE
)

/** http(s) 웹 URL 형식이 유효한지 검증. 빈 문자열·공백만 있는 문자열은 false */
fun String.isValidWebUrl(): Boolean = isNotBlank() && matches(WEB_URL_REGEX)
