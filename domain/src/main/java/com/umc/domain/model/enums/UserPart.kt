package com.umc.domain.model.enums

/**
 * 챌린저의 파트와 트랙
 *
 * **enum 이름이 곧 서버와 주고받는 값**이다. 서버는 둘을 다른 enum 으로 갖고 있고, 여기서는 한 곳에 모아 둔다.
 *
 * - 파트 `ChallengerPart` (8종) → [parts]
 *   PLAN, DESIGN, WEB, ANDROID, IOS, NODEJS, SPRINGBOOT, ADMIN
 * - 트랙 `ChallengerTrack` (5종) → [tracks]
 *   PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER, INFRA_PLUS
 *
 * PLAN 과 DESIGN 은 파트이면서 트랙이라 항목을 하나만 둔다.
 *
 * **⚠️ 파트를 주고받는 API 에 트랙 값을 넣으면 서버가 거부한다.** 그래서 목록을 섞지 않는다.
 * 파트 자리에는 [parts] · [selectable] · [filters] 만 쓰고, 트랙 자리에는 [tracks] 만 쓴다.
 *
 * 파트·트랙에 관한 것은 화면·매퍼 어디서든 이 enum 하나만 쓴다.
 * - 서버로 보낼 값이 필요하면 [serverValue]
 * - 화면에 보여줄 이름이 필요하면 [label]
 *
 * 화면마다 `when` 으로 라벨을 다시 만들거나 문자열을 직접 적지 말 것.
 * 예전에는 study/act/community 가 각자 라벨을 만들어 같은 파트가 "SpringBoot" · "Spring Boot" · "Spring"
 * 세 가지로 보였고, 스터디 그룹 수정에서는 서버에 없는 "SPRING" 을 보내는 버그까지 있었다.
 *
 * 선언 순서는 서버의 `sortOrder` 와 맞춘다(파트 0~7, 트랙 0~4).
 * 목록·필터·그룹핑이 이 순서를 그대로 쓰므로, 어긋나면 화면 정렬이 서버와 달라진다.
 */
enum class UserPart(val label: String) {
    // ── 파트 겸 트랙 ──
    PLAN("PM"),
    DESIGN("Design"),

    // ── 파트 전용 ──
    WEB("Web"),
    ANDROID("Android"),
    IOS("iOS"),
    NODEJS("Node.js"),
    SPRINGBOOT("Spring"),
    ADMIN("Admin"),

    // ── 트랙 전용 ──
    WEB_PRODUCT_ENGINEER("Web"),
    MOBILE_PRODUCT_ENGINEER("Mobile"),
    INFRA_PLUS("Infra"),

    /** 서버가 파트·트랙을 안 내려준 경우. TRACK 학습 유형 기수의 `part` 처럼 값 자체가 없을 수 있다. */
    UNKNOWN("미정");

    /** 서버 요청·응답에 쓰는 값 */
    val serverValue: String get() = name

    companion object {
        /** 서버 `ChallengerPart` 와 1:1. 파트를 주고받는 API 는 이 목록 안에서만 골라야 한다. */
        val parts: List<UserPart>
            get() = listOf(PLAN, DESIGN, WEB, ANDROID, IOS, NODEJS, SPRINGBOOT, ADMIN)

        /** 서버 `ChallengerTrack` 과 1:1 */
        val tracks: List<UserPart>
            get() = listOf(PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER, INFRA_PLUS)

        /** 사용자가 고를 수 있는 전체 파트 (운영진 포함) */
        val selectable: List<UserPart> get() = parts

        /** 목록 화면의 파트 필터 (운영진 제외) */
        val filters: List<UserPart> get() = parts.filter { it != ADMIN }

        /**
         * 문자열을 파트·트랙으로 바꾼다.
         *
         * 서버 값("SPRINGBOOT", "MOBILE_PRODUCT_ENGINEER")이든 화면 라벨("Spring", "Mobile")이든 받아주며,
         * 대소문자·공백·밑줄·점 차이는 무시한다. 모르는 값이면 [UNKNOWN] 을 돌려주므로
         * 서버에 새 값이 생겨도 앱이 죽지 않는다. (`valueOf` 를 직접 쓰지 말 것)
         *
         * 라벨 "Web" 은 파트 [WEB] 과 트랙 [WEB_PRODUCT_ENGINEER] 가 함께 쓴다.
         * 서버 값으로는 이름이 달라 구분되고, 라벨만 들어온 경우에는 먼저 선언된 파트 [WEB] 으로 본다.
         */
        fun from(value: String?): UserPart {
            if (value.isNullOrBlank()) return UNKNOWN

            val key = value.partKey()
            return entries.firstOrNull { it.name.partKey() == key }
                ?: entries.firstOrNull { it.label.partKey() == key }
                ?: UNKNOWN
        }

        /**
         * 트랙을 우선해서 하나의 값으로 고른다.
         *
         * 같은 응답에 `track` 과 `part` 가 함께 오는 API 가 여럿이다. 학습 유형이 TRACK 인
         * 기수는 트랙만, PART 인 기수는 파트만 채워져 오므로 트랙이 있으면 트랙을 쓴다.
         * 둘 다 없으면 [UNKNOWN] 이다.
         */
        fun resolve(track: String?, part: String?): UserPart =
            from(track).takeIf { it != UNKNOWN } ?: from(part)

        /** 표기 차이(대소문자·공백·밑줄·점)를 지운 비교용 키 */
        private fun String.partKey(): String =
            trim().lowercase().filterNot { it == '_' || it == ' ' || it == '.' }
    }
}
