package com.umc.presentation

import android.net.Uri
import android.content.Context
import android.content.SharedPreferences

/**
 * 외부 링크(딥링크)로 앱을 처음 열었을 때, 인증 판정이 끝나 홈에 도착할 때까지 목적지를 보관한다.
 *
 * 딥링크를 NavController 가 바로 처리하면 스플래시(인증 게이트)를 건너뛰어 로그아웃 상태에서도
 * 목적지 화면이 열린다. 그래서 MainActivity 가 링크를 여기로 옮기고, 앱은 항상 스플래시부터 시작한다.
 * 로그인/코드 등록을 마치고 홈에 도착하면 MainNavHost 가 [consume] 으로 꺼내 원래 목적지로 이동시킨다.
 */
object PendingDeepLink {
    private var preferences: SharedPreferences? = null
    @Volatile
    var uri: Uri? = null
        set(value) {
            if (field == value) return
            field = value
            preferences?.edit()?.putString("uri", value?.toString())?.apply()
        }

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences("pending_deep_link", Context.MODE_PRIVATE)
        if (uri == null) uri = preferences?.getString("uri", null)?.let(Uri::parse)
    }

    /** 보관 중인 링크를 한 번만 꺼낸다. */
    fun consume(): Uri? {
        val pending = uri
        uri = null
        return pending
    }
}
