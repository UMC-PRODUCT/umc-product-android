package com.umc.domain.model.enums

/**
 * 회원가입 진입 경로.
 * - SOCIAL: 소셜(카카오/구글) 로그인 후 회원가입 → member/register/oauth 호출
 * - EMAIL: 이메일 회원가입 → member/register/email 호출
 */
enum class SignUpType { SOCIAL, EMAIL }
