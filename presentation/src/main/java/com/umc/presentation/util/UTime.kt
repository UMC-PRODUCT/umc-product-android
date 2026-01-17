package com.umc.presentation.util

object UTime {
    /**
     * 시작 시간과 종료 시간을 받아 "HH:mm - HH:mm" 형태의 문자열로 반환합니다.
     */
    fun formatDuration(startTime: String, endTime: String): String {
        return "$startTime - $endTime"
    }
}