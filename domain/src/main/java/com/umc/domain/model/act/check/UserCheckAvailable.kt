package com.umc.domain.model.act.check

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus

/**
 * 현재 출석 가능한 세션 정보
 */
data class UserCheckAvailable(
    val id: Long,
    val sheetId: Long,
    val title: String,
    val tags: List<CategoryType>?,
    val startTime: String,
    val endTime: String,
    val status: CheckAvailableStatus,
    val rawStatus: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isLocationCertified: Boolean? = null
)