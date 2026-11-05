package com.umc.presentation.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/** screen_view 를 보내는 곳. 테스트에서는 가짜 구현으로 바꿔 끼운다 */
interface ScreenViewSink {
    fun logScreenView(screenName: String)
}

class FirebaseScreenViewSink @Inject constructor(
    @ApplicationContext context: Context,
) : ScreenViewSink {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logScreenView(screenName: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            }
        )
    }
}

/**
 * Firebase 전송 구현을 연결한다.
 * :app 의 KSP 가 이 모듈의 타입을 풀지 못해서(:app/di 에 두면 kspDevDebugKotlin 실패) 구현과 같은 모듈에 둔다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Singleton
    @Binds
    abstract fun bindScreenViewSink(
        screenViewSink: FirebaseScreenViewSink,
    ): ScreenViewSink
}
