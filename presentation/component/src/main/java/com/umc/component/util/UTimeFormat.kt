package com.umc.component.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**시간 및 날짜 관련 파싱 함수들이 정의된 Util 입니다.**/
object UTimeFormat{

    /**
     * LocalDate를 시스템 기본 타임존 기준 밀리초(Long)로 변환
     */
    fun LocalDate.toMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * 밀리초(Long)를 시스템 기본 타임존 기준 LocalDate로 변환
     */
    fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

}