package com.umc.data.response.remoteconfig

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.remoteconfig.RemoteNotice
import com.umc.domain.model.remoteconfig.RemoteNoticeTemplate

data class AppConfigResponse(
    @SerializedName("version") val version: Int?,
    @SerializedName("notices") val notices: List<RemoteNoticeResponse>?,
) {
    /**
     * 앱이 아는 스키마 버전일 때만 읽는다
     *
     * 설정 형식이 바뀐 파일을 구버전 앱이 잘못 해석해 엉뚱한 안내를 띄우지 않도록, 모르는 버전이면 하나도 띄우지 않는다.
     */
    fun toDomain(): List<RemoteNotice> {
        if (version != SUPPORTED_VERSION) return emptyList()
        return notices.orEmpty().mapNotNull { it.toDomain() }
    }

    companion object {
        private const val SUPPORTED_VERSION = 1
    }
}

data class RemoteNoticeResponse(
    @SerializedName("screen") val screen: String?,
    @SerializedName("enabled") val enabled: Boolean?,
    @SerializedName("template") val template: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("until") val until: String?,
) {
    // 필수 값이 빠진 항목은 버린다. 저장소의 스키마 검사를 통과한 파일이면 여기서 걸러질 일은 없다
    fun toDomain(): RemoteNotice? {
        if (screen.isNullOrBlank() || title.isNullOrBlank() || body.isNullOrBlank()) return null

        return RemoteNotice(
            screen = screen,
            enabled = enabled == true,
            template = RemoteNoticeTemplate.from(template),
            title = title,
            body = body,
            until = until,
        )
    }
}
