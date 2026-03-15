package com.umc.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object UDomainFormat {

    /**
     * 서버 UTC DateTime 문자열(예: 2026-03-15T12:49:13.165158Z)을
     * 한국 시간(KST) 기준 날짜/시간 문자열로 변환한다.
     */
    fun String.parseDateTime(): Pair<String, String> {
        if (this.isBlank()) return "" to ""

        return try {
            val input = this.trim().removeSurrounding("\"")
            val kstDateTime = Instant.parse(input).atZone(ZoneId.of("Asia/Seoul"))

            val date = kstDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA))
            val time = kstDateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA))
            date to time
        } catch (_: Exception) {
            "" to ""
        }
    }
}

//TODO 삭제
//object UDomainFormat {
//
//    /**
//     * 서버의 ISO DateTime 문자열(2026-02-07T05:24:54) -> "2026.02.07" , "05:24:54"
//     * 처럼 날짜와 시간 String으로 분리하는 확장 함수
//     *
//     * TODO: 02-24 추가 : UTC 판별을 해서 시간까지 쫘악 바꿔주는 로직 추가
//     */
//    fun String.parseDateTime(): Pair<String, String> {
//        if (this.isBlank()) return Pair("", "")
//
//        return try{
//            //일단 Z로 안끝나면 붙여주기
//            val normalizedInput = if (!this.endsWith("Z")) "${this}Z" else this
//
//            //ISO_DATE_TIME 형식으로 파싱
//            val utcTime = ZonedDateTime.parse(normalizedInput)
//
//            //사용자의 현재 기기 시간대(Local)로 변환
//            val localTime = utcTime.withZoneSameInstant(ZoneId.systemDefault())
//
//            //날짜와 시간을 원하는 포맷으로 분리
//            val date = localTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
//            val time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
//
//            Pair(date, time)
//
//        } catch (e: Exception) {
//            // 파싱 실패 시 예외 처리 (기존 String 자르기 방식으로 폴백)
//            val date = this.split("T").getOrNull(0)?.replace("-", ".") ?: ""
//            val time = this.split("T").getOrNull(1)?.take(5) ?: ""
//            Pair(date, time)
//        }
//
//
//
//    }
//
//
//
//}