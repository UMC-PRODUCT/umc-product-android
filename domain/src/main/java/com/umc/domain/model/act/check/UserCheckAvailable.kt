package com.umc.domain.model.act.check

import com.umc.domain.model.enums.CheckAvailableStatus

/**
 * 현재 출석 가능한 세션 정보
 */
data class UserCheckAvailable(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val admin: String,
    val status: CheckAvailableStatus,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isLocationCertified: Boolean? = null
)