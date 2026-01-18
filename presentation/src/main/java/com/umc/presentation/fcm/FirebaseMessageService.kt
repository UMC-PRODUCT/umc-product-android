package com.umc.presentation.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint


/**
 * 백그라운드 : 시스템 자체가 알람을 수신받아 올림
 * 포그라운드 : 유저가 앱을 쓰는 중이므로, 자체 로직(onMessageReceived)으로 해결
 * **/

@AndroidEntryPoint
class FirebaseMessageService : FirebaseMessagingService() {

    // 새로운 토큰이 생성될 때 호출
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("log_fcm", "획득한 토큰: $token")
        /** 나중에 서버 API가 준비되면 여기로 토큰 송신**/

    }

    // 푸시 메시지를 받았을 때 호출
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


    }



}