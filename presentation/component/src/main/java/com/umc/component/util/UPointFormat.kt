package com.umc.component.util

/**상벌점 점수 표시. 서버 배점은 정수이거나 0.5 단위(구형 베스트 워크북 -0.5)라 소수점을 자르지 않는다.**/
object UPointFormat {

    /** 2.0 -> "2", -0.5 -> "-0.5", -0.0 -> "0" **/
    fun Double.toPointText(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()

    /** 이력 점수 배지용. 양수에만 + 를 붙인다. 2.0 -> "+2", -0.5 -> "-0.5", 0.0 -> "0" **/
    fun Double.toSignedPointText(): String =
        if (this > 0) "+${toPointText()}" else toPointText()
}
