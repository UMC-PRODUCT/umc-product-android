package com.umc.domain.model.enums

/**
 * 이메일 인증 발송 목적.
 * 서버 스펙상 발송 요청 바디의 `purpose` 로 전달되며, 코드 검증으로 발급되는
 * emailVerificationToken 의 사용처도 이 값에 묶인다.
 * - REGISTER: 회원가입 흐름 (현재 앱에서 사용)
 * - PASSWORD_RESET: 비밀번호 초기화 흐름 (현재 앱 미사용)
 */
enum class EmailVerifyPurpose { REGISTER, PASSWORD_RESET }
