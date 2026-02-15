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


    /**
     * 서버의 ISO DateTime 문자열(2026-02-07T05:24:54) -> "2026.02.07" , "05:24"
     * 후에 instant로 바뀌어도, T만 존재하면 분리가 가능하다.
     * 처럼 날짜와 시간 String으로 분리하는 확장 함수
     */
    fun parseDateTime(date: String): Pair<String, String> {
        if (date.isBlank()) return Pair("", "")

        val dateTimeParts = date.split("T")
        // 2026-02-07 -> 2026.02.07
        val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
        // 13:05:03 -> 13:05:03
        val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""

        return Pair(date, time)
    }


}