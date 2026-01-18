package com.umc.presentation.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint


/**
 * 백그라운드 : 시스템 자체가 알람을 수신받아 올림
 * 포그라운드 : 유저가 앱을 쓰는 중이므로, 자체 로직(onMessageReceived)으로 해결
 * **/

@AndroidEntryPoint
class FirebaseMessageService : FirebaseMessagingService() {

    //새로운 토큰이 생성될 때 호출
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("log_fcm", "획득한 토큰: $token")
        /** 나중에 서버 API가 준비되면 여기로 토큰 송신**/

    }

    //푸시 메시지를 받았을 때 호출
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // 알림(Notification) 페이로드가 포함된 경우
        message.notification?.let {
            Log.d("log_fcm", "데이터 없는 알람 수신")
            val title = it.title ?: "데이터가 없어요"
            val body = it.body ?: "그렇다네요"
            showNotification(title, body)
        }

        // 데이터(Data) 페이로드가 포함된 경우
        if (message.data.isNotEmpty()) {
            Log.d("log_fcm", "데이터 있는 알람 수신")
            val title = message.data["title"] ?: "데이터가 있대요"
            val body = message.data["body"] ?: "글헣다네욘"
            showNotification(title, body)
        }

    }

    //포그라운드에서 메시지 받았을 때 처리
    private fun showNotification(title: String, body: String){
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


        // 알람 빌더 생성
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.umc.presentation.R.drawable.ic_logo_umc) //일단 UMC 로고 임시
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // 클릭하면 알림이 자동으로 사라짐
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 알람 띄우기 (ID를 시스템으로 설정해서 쌓이도록)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())

    }



}