package com.umc.component.fcm

import android.app.PendingIntent
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.umc.domain.model.home.NotificationItem
import com.umc.domain.usecase.appDataStore.notification.AddNotificationUseCase
import com.umc.component.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


/**
 * 백그라운드 : 시스템 자체가 알람을 수신받아 올림
 * 포그라운드 : 유저가 앱을 쓰는 중이므로, 자체 로직(onMessageReceived)으로 해결
 * **/

@AndroidEntryPoint
class FirebaseMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var addNotificationUseCase: AddNotificationUseCase

    @Inject lateinit var tokenSynchronizer: FcmTokenSynchronizer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //새로운 토큰이 생성될 때 호출
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenSynchronizer.sync(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val route = CommunityNotification.parse(message.data)
        val eventId = route?.eventId ?: message.messageId
        if (eventId != null) {
            val prefs = getSharedPreferences("fcm_events", Context.MODE_PRIVATE)
            synchronized(FirebaseMessageService::class.java) {
                val seen = prefs.getStringSet("displayed", emptySet()).orEmpty()
                if (eventId in seen) return
                prefs.edit().putStringSet("displayed", (seen.toList().takeLast(199) + eventId).toSet()).commit()
            }
        }
        val title = message.data["title"] ?: message.notification?.title ?: "UMC"
        val body = message.data["body"] ?: message.notification?.body ?: "새 알림이 도착했어요."
        saveNotificationToDataStore(title, body)
        if (route != null && ActiveCommunityThread.threadId == route.threadId) return
        showNotification(title, body, route)
    }

    // DataStore에 알림 저장
    private fun saveNotificationToDataStore(title: String, body: String) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val notificationItem = NotificationItem(
            title = title,
            content = body,
            date = currentTime
        )

        serviceScope.launch {
            try {
                addNotificationUseCase(notificationItem)
                Log.d("log_fcm", "알림 DataStore 저장 성공: $title")
            } catch (e: Exception) {
                Log.e("log_fcm", "알림 DataStore 저장 실패: ${e.message}")
            }
        }
    }

    //포그라운드에서 메시지 받았을 때 처리
    private fun showNotification(title: String, body: String, route: CommunityNotification?) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val channelId = "umc_product_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //안드로이드 8(오레오) 이상 시 알림 채널 설정 (제어 용도)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "기본 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }


        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            action = Intent.ACTION_VIEW
            data = route?.uri()
            route?.eventId?.let { putExtra("notificationEventId", it) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        // 알람 빌더 생성
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_app_store_logo) //일단 UMC 로고 임시
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent)
            .setAutoCancel(true) // 클릭하면 알림이 자동으로 사라짐
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 알람 띄우기 (ID를 시스템으로 설정해서 쌓이도록)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}