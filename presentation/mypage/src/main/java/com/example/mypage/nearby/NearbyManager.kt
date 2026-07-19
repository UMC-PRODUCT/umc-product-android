package com.example.mypage.nearby

import android.content.Context
import android.os.Build
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.umc.domain.model.mypage.UserCard

class NearbyManager(
    context: Context,
    private val onEvent: (NearbyManagerEvent) -> Unit
) {
    private val client = Nearby.getConnectionsClient(context)
    //다른 애플리케이션이랑 겹치지 않도록 패키지 이름 사용
    private val SERVICE_ID = "com.example.mypage.nearby"

    //endpoint 이름 = 기기 모델명
    private val localEndpointName = Build.MODEL

    // 1:N으로 유저탐색
    fun startDiscovery() {
        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()
        client.startDiscovery(SERVICE_ID, discoveryCallback, options)
    }

    // 연결 시도
    fun requestConnection(endpointId: String) {
        client.requestConnection(localEndpointName, endpointId, lifecycleCallback)
            // 연결 과정에서 발생하는 에러 메시지 추가
            .addOnFailureListener { exception ->
                val message = if (exception is com.google.android.gms.common.api.ApiException) {
                    when (exception.statusCode) {
                        ConnectionsStatusCodes.STATUS_ENDPOINT_UNKNOWN -> "기기를 찾을 수 없습니다."
                        ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED -> "네트워크에 연결되지 않았습니다."
                        ConnectionsStatusCodes.STATUS_BLUETOOTH_ERROR -> "블루투스 오류가 발생했습니다."
                        else -> "연결 오류: ${exception.statusCode}"
                    }
                } else {
                    "알 수 없는 오류 발생"
                }
                // 에러 메시지를 UI에 전달 (이벤트를 새롭게 추가해야 합니다)
                onEvent(NearbyManagerEvent.Error(message))
            }
    }

    // 인증 후 연결 승인
    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
    }

    // 데이터 전송
    fun sendUserCard(endpointId: String, card: UserCard) {
        client.sendPayload(endpointId, Payload.fromBytes(card.toJson().toByteArray()))
    }

    // 모든 통신 정리
    fun stopAll() {
        client.stopDiscovery()
        client.stopAdvertising()
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            // 구글에서 자동 생성한 6자리 인증번호 이벤트 전달
            onEvent(NearbyManagerEvent.AuthVerification(id, info.authenticationDigits))
        }

        override fun onConnectionResult(id: String, res: ConnectionResolution) {
            if (res.status.isSuccess) {
                client.stopDiscovery() // 연결 성공 시 탐색 중지
                onEvent(NearbyManagerEvent.ConnectionSuccess(id))
            }
        }

        override fun onDisconnected(id: String) {}
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            val json = String(payload.asBytes()!!)
            onEvent(NearbyManagerEvent.UserCardReceived(UserCard.fromJson(json)))
        }

        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {}
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            onEvent(NearbyManagerEvent.EndpointFound(id, info.endpointName))
        }

        override fun onEndpointLost(id: String) {}
    }
}



sealed class NearbyManagerEvent {
    data class EndpointFound(val id: String, val name: String) : NearbyManagerEvent()
    data class AuthVerification(val id: String, val code: String) : NearbyManagerEvent()
    data class ConnectionSuccess(val id: String) : NearbyManagerEvent()
    data class UserCardReceived(val card: UserCard) : NearbyManagerEvent()
    data class Error(val message: String) : NearbyManagerEvent()
}