package com.umc.component.analytics

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.umc.domain.analytics.AnalyticsLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics 로 이벤트를 보낸다. 수집 여부는 :app 매니페스트(prod flavor 만 켬)가 정한다.
 *
 * 디버그 빌드에서는 같은 내용을 logcat(태그 [LOG_TAG])에도 남긴다.
 * dev flavor 는 수집이 꺼져 Firebase 로그에 이벤트 이름이 찍히지 않아서, QA 가 `adb logcat -s UMC_ANALYTICS` 로 확인한다.
 */
class FirebaseAnalyticsLogger @Inject constructor(
    @ApplicationContext context: Context,
) : AnalyticsLogger {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val debugLogging = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override fun log(name: String, params: Map<String, String>) {
        analytics.logEvent(
            name,
            Bundle().apply { params.forEach { (key, value) -> putString(key, value) } }
        )
        if (debugLogging) Log.d(LOG_TAG, if (params.isEmpty()) name else "$name $params")
    }

    private companion object {
        const val LOG_TAG = "UMC_ANALYTICS"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsLoggerModule {

    @Singleton
    @Binds
    abstract fun bindAnalyticsLogger(
        analyticsLogger: FirebaseAnalyticsLogger,
    ): AnalyticsLogger
}
