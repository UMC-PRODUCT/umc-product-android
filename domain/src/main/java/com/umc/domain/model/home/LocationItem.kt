package com.umc.domain.model.home

data class LocationItem(
    val title: String,   // 서울역
    val address: String,  // 서울특별시 용산구...
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
    )