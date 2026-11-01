package com.umc.presentation

import android.net.Uri
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 인증 전에는 링크를 보관하고, 인증된 화면에서는 새 알림을 즉시 관찰할 수 있게 한다. */
object PendingDeepLink {
    private var preferences: SharedPreferences? = null
    private val pendingUri = MutableStateFlow<Uri?>(null)
    val updates = pendingUri.asStateFlow()

    var uri: Uri?
        get() = pendingUri.value
        set(value) {
            if (pendingUri.value == value) return
            pendingUri.value = value
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
