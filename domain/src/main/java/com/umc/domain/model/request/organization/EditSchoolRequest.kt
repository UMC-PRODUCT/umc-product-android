package com.umc.domain.model.request.organization

import kotlinx.serialization.Serializable

@Serializable
data class EditSchoolRequest(
    val schoolName: String,
    val chapterId: Int,
    val remark: String,
    val logoImageId: String
)