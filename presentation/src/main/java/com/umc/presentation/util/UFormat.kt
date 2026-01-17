package com.umc.presentation.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object UFormat {
    /**
     * 시작 시간과 종료 시간을 받아 "HH:mm - HH:mm" 형태의 문자열로 반환합니다.
     */
    fun formatDuration(startTime: String, endTime: String): String {
        return "$startTime - $endTime"
    }

    /**
     * "yyyy-MM-dd" 형식의 날짜를 "yyyy.MM.dd (요일)" 형태로 변환합니다.
     */
    fun formatDate(date: String): String {
        return try {
            val localDate = LocalDate.parse(date)
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
            localDate.format(formatter)
        } catch (e: Exception) {
            date
        }
    }
}