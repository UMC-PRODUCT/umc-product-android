package com.umc.domain.model.enums

/**
 * 챌린저의 파트
 *
 * **enum 이름이 곧 서버와 주고받는 값**이다. 서버 `ChallengerPart` (10종) 와 1:1 로 맞춘다.
 *
 * 파트에 관한 것은 화면·매퍼 어디서든 이 enum 하나만 쓴다.
 * - 서버로 보낼 값이 필요하면 [serverValue]
 * - 화면에 보여줄 이름이 필요하면 [label]
 *
 * 화면마다 `when` 으로 라벨을 다시 만들거나 문자열을 직접 적지 말 것.
 * 예전에는 study/act/community 가 각자 라벨을 만들어 같은 파트가 "SpringBoot" · "Spring Boot" · "Spring"
 * 세 가지로 보였고, 스터디 그룹 수정에서는 서버에 없는 "SPRING" 을 보내는 버그까지 있었다.
 *
 * 선언 순서는 서버의 `sortOrder` 와 맞춘다(파트 0~9).
 * 목록·필터·그룹핑이 이 순서를 그대로 쓰므로, 어긋나면 화면 정렬이 서버와 달라진다.
 *
 * 11기부터 파트는 PM · Design · Web · Mobile 네 가지만 쓴다. WEB, ANDROID, IOS, NODEJS, SPRINGBOOT
 * 는 지난 기수 기록에 그대로 남아 있어(예: 10기 ANDROID, 9기 IOS) 표시·파싱용으로만 남겨 두고,
 * 사용자가 고르는 자리([selectable] · [filters])에는 넣지 않는다.
 *
 * 인프라는 파트가 아니다. 챌린저마다 따로 내려오는 `infra` 여부 값이라 여기에 두지 않는다.
 *
 * 서버 리크루팅(모집) 도메인에는 `ChallengerTrack`(PLAN, DESIGN, WEB_PRODUCT_ENGINEER,
 * MOBILE_PRODUCT_ENGINEER, INFRA_PLUS)이 따로 남아 있지만 앱은 모집 API 를 쓰지 않아 여기 담지 않는다.
 */
enum class UserPart(val label: String) {
    PLAN("PM"),
    DESIGN("Design"),
    WEB("Web"),
    ANDROID("Android"),
    IOS("iOS"),
    NODEJS("Node.js"),
    SPRINGBOOT("Spring"),
    ADMIN("Admin"),
    WEB_PRODUCT_ENGINEER("Web PE"),
    MOBILE_PRODUCT_ENGINEER("Mobile PE"),

    /** 서버가 파트를 안 내려준 경우 */
    UNKNOWN("미정");

    /** 서버 요청·응답에 쓰는 값 */
    val serverValue: String get() = name

    companion object {
        /** 서버 `ChallengerPart` 와 1:1. 파트를 주고받는 API 는 이 목록 안에서만 골라야 한다. */
        val parts: List<UserPart>
            get() = listOf(
                PLAN,
                DESIGN,
                WEB,
                ANDROID,
                IOS,
                NODEJS,
                SPRINGBOOT,
                ADMIN,
                WEB_PRODUCT_ENGINEER,
                MOBILE_PRODUCT_ENGINEER
            )

        /**
         * 지금 쓰는 파트. 화면에서 고르거나 거르는 자리는 모두 이 목록만 쓴다.
         *
         * 지난 기수의 WEB · ANDROID · IOS · NODEJS · SPRINGBOOT 는 여기에 없다.
         * 그 기록을 화면에 그릴 때는 [from] 이 그대로 변환해 준다.
         */
        val current: List<UserPart>
            get() = listOf(PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER)

        /** 사용자가 고를 수 있는 파트 */
        val selectable: List<UserPart> get() = current

        /** 목록 화면의 파트 필터 */
        val filters: List<UserPart> get() = current

        /** 10기까지 쓰던 파트. 지난 기수 공지를 거를 때만 쓴다. */
        val legacy: List<UserPart>
            get() = listOf(PLAN, DESIGN, WEB, ANDROID, IOS, NODEJS, SPRINGBOOT)

        /**
         * 그 기수에서 고를 수 있는 파트.
         *
         * 11기에 파트 체계가 바뀌어 같은 화면이라도 보고 있는 기수에 따라 목록이 달라진다.
         * 10기 공지를 Web PE 로 거르거나 11기 공지를 Android 로 걸러도 결과가 비기 때문에,
         * 애초에 그 기수에 없는 파트는 보여주지 않는다.
         *
         * @param generation 기수 번호(11기면 11). 0 이하면 기수를 모르는 경우로 보고 둘 다 준다.
         */
        fun filtersOf(generation: Int): List<UserPart> = when {
            generation <= 0 -> (legacy + current).distinct()
            generation >= FIRST_TRACK_GENERATION -> current
            else -> legacy
        }

        /** 파트가 트랙 기준으로 바뀐 첫 기수 */
        private const val FIRST_TRACK_GENERATION = 11

        /**
         * 문자열을 파트로 바꾼다.
         *
         * 서버 값("SPRINGBOOT", "MOBILE_PRODUCT_ENGINEER")이든 화면 라벨("Spring", "Mobile PE")이든 받아주며,
         * 대소문자·공백·밑줄·점 차이는 무시한다. 모르는 값이면 [UNKNOWN] 을 돌려주므로
         * 서버에 새 값이 생겨도 앱이 죽지 않는다. (`valueOf` 를 직접 쓰지 말 것)
         *
         * 라벨이 겹치는 파트가 생기면 지금 쓰는 파트([current])를 먼저 본다.
         */
        fun from(value: String?): UserPart {
            if (value.isNullOrBlank()) return UNKNOWN

            val key = value.partKey()
            return entries.firstOrNull { it.name.partKey() == key }
                ?: current.firstOrNull { it.label.partKey() == key }
                ?: entries.firstOrNull { it.label.partKey() == key }
                ?: UNKNOWN
        }

        /** 표기 차이(대소문자·공백·밑줄·점)를 지운 비교용 키 */
        private fun String.partKey(): String =
            trim().lowercase().filterNot { it == '_' || it == ' ' || it == '.' }
    }
}
