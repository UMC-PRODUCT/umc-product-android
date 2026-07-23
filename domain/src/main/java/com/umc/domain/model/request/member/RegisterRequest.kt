package com.umc.domain.model.request.member

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val oAuthVerificationToken: String,
    val name: String,
    val nickname: String,
    val emailVerificationToken: String,
    val schoolId: Int,
    val profileImageId: String?,
    val termsAgreements: List<TermsAgreement> = listOf(
        TermsAgreement(termsId = 19, isAgreed = true),
        TermsAgreement(termsId = 18, isAgreed = true)
    ),
    // Apple 로그인 전용 필드. Android 에서는 빈 값으로 전달
    val appleRefreshToken: String = "",
    val appleClientId: String = "",
)

@Serializable
data class TermsAgreement(
    val termsId: Int = 1,
    val isAgreed: Boolean = true
)
