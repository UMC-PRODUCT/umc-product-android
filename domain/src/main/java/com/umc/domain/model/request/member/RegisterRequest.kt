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
        TermsAgreement(termsId = 14, isAgreed = true),
        TermsAgreement(termsId = 13, isAgreed = true),
        TermsAgreement(termsId = 15, isAgreed = true)
    )

)

@Serializable
data class TermsAgreement(
    val termsId: Int = 1,
    val isAgreed: Boolean = true
)
