package com.umc.component.util

// 이메일 형식 검증 정규식.
// 매 호출마다 컴파일되지 않도록 파일 최상단에서 한 번만 생성
private val EMAIL_REGEX = Regex(
    pattern = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+",
    option = RegexOption.IGNORE_CASE
)

/** 이메일 형식이 유효한지 검증. 빈 문자열·공백만 있는 문자열은 false */
fun String.isValidEmail(): Boolean = isNotBlank() && matches(EMAIL_REGEX)
