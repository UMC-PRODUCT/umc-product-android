package com.umc.domain.analytics

/**
 * 이용 지표 이벤트를 남긴다. 구현은 Firebase Analytics(:presentation:component)다.
 *
 * - 이벤트·파라미터 이름은 iOS 와 같게 쓴다. 두 플랫폼을 한 이름으로 함께 집계하기 위해서다.
 * - 파라미터 값은 iOS 와 같은 문자열로 보낸다(Boolean 은 "true"/"false").
 * - 회원 ID, 제목, 메시지 내용처럼 사람을 특정하거나 값 종류가 많은 정보는 넣지 않는다.
 */
interface AnalyticsLogger {
    fun log(name: String, params: Map<String, String> = emptyMap())
}
