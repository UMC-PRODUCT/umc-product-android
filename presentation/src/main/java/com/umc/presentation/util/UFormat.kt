package com.umc.presentation.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object UFormat {
    /**
     * 시작 시간과 종료 시간을 받아 "HH:mm - HH:mm" 형태의 문자열로 반환합니다.
     * 입력 예: "10:00:00", "22:00:00" -> 출력 예: "10:00 - 22:00"
     */
    fun formatDuration(startTime: String, endTime: String): String {
        return try {
            // "HH:mm:ss" 형태로 오는 서버 데이터를 위해 파서 정의
            val inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val start = LocalTime.parse(startTime, inputFormatter).format(outputFormatter)
            val end = LocalTime.parse(endTime, inputFormatter).format(outputFormatter)

            "$start - $end"
        } catch (e: Exception) {
            // 파싱 실패 시 예외 처리 (콜론 개수로 문자열 직접 자르기)
            val start = if (startTime.count { it == ':' } >= 2) startTime.substringBeforeLast(":") else startTime
            val end = if (endTime.count { it == ':' } >= 2) endTime.substringBeforeLast(":") else endTime
            "$start - $end"
        }
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