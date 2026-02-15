package com.umc.domain.model.school

import java.io.Serializable

data class SchoolInfo(
    val schoolId: Int = -1,
    val schoolName: String = "",
): Serializable
