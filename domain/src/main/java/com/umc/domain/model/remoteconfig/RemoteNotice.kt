package com.umc.domain.model.remoteconfig

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * 원격 설정 저장소(UMC-PRODUCT/umc-product-android-config)에서 받은 화면별 안내
 *
 * 앱을 새로 배포하지 않고 특정 화면에 안내 다이얼로그를 켜고 끄는 데 쓴다.
 * 값의 규칙은 그 저장소의 schema.json 이 기준이다.
 *
 * @property screen 띄울 화면. 앱 경로 이름(MainDestination) 그대로다. 예: `EmailSignUp`. [ALL_SCREENS] 이면 모든 화면
 * @property until 이 날짜(포함)까지만 띄운다. `YYYY-MM-DD` 형식이고, 없으면 기한이 없다
 */
data class RemoteNotice(
    val screen: String,
    val enabled: Boolean,
    val template: RemoteNoticeTemplate,
    val title: String,
    val body: String,
    val until: String?,
) {
    /**
     * 지금 띄워도 되는지
     *
     * 꺼져 있거나, 앱이 모르는 모양이거나, 종료일이 지났으면 띄우지 않는다.
     * 종료일 형식이 깨져 있으면 기한을 판단할 수 없으니 띄우지 않는 쪽을 택한다.
     */
    fun isShowable(today: LocalDate): Boolean {
        if (!enabled || template == RemoteNoticeTemplate.UNKNOWN) return false
        if (until.isNullOrBlank()) return true

        val lastDay = try {
            LocalDate.parse(until)
        } catch (e: DateTimeParseException) {
            return false
        }
        return !today.isAfter(lastDay)
    }

    /** 이 안내가 [currentScreen] 화면 대상인지 */
    fun targets(currentScreen: String): Boolean =
        screen == ALL_SCREENS || screen == currentScreen

    companion object {
        /** 모든 화면을 뜻하는 값. 점검 안내처럼 앱 전체에 띄울 때 쓴다 */
        const val ALL_SCREENS = "ALL"
    }
}

/** 다이얼로그 모양. 앱이 모르는 값은 [UNKNOWN] 으로 받아 무시한다 (새 모양이 추가돼도 구버전 앱이 깨지지 않도록) */
enum class RemoteNoticeTemplate {
    INFO,
    BLOCKING,
    UNKNOWN;

    companion object {
        fun from(value: String?): RemoteNoticeTemplate =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
