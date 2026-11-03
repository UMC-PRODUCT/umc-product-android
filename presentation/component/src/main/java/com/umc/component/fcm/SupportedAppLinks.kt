package com.umc.component.fcm

import java.net.URI
import java.net.URLDecoder

/** 네비게이션 그래프에 등록된 스레드/명함 주소만 앱 내부 목적지로 허용한다. */
fun isSupportedAppLink(value: String): Boolean = runCatching {
    val uri = URI(value)
    val query = uri.rawQuery.orEmpty().split('&').mapNotNull { pair ->
        val key = pair.substringBefore('=')
        val rawValue = pair.substringAfter('=', "")
        if (key.isEmpty()) null else key to URLDecoder.decode(rawValue, "UTF-8")
    }.toMap()
    fun validId(value: String?): Boolean = value?.matches(Regex("[1-9][0-9]*")) == true
    when {
        uri.scheme == "umc" && uri.rawAuthority == "thread" ->
            (uri.path.isNullOrEmpty() && validId(query["threadId"])) ||
                (uri.path.orEmpty().startsWith('/') && validId(uri.path.drop(1)))
        uri.scheme == "umc" && uri.rawAuthority == "card" ->
            uri.path.isNullOrEmpty() && validId(query["memberId"])
        uri.scheme == "https" && uri.rawAuthority == "api.university.neordinary.com" -> when (uri.path) {
            "/community/threads" -> validId(query["threadId"])
            "/community/threads/card" -> validId(query["memberId"])
            else -> false
        }
        else -> false
    }
}.getOrDefault(false)
