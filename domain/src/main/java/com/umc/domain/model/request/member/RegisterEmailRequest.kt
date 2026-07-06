package com.umc.domain.model.request.member

import kotlinx.serialization.Serializable

@Serializable
data class RegisterEmailRequest(
    val rawPassword: String,
    val name: String,
    val nickname: String,
    val emailVerificationToken: String,
    val schoolId: Int,
    val termsAgreements: List<TermsAgreement> = listOf(
        TermsAgreement(termsId = 19, isAgreed = true),
        TermsAgreement(termsId = 18, isAgreed = true)
    ),
)
