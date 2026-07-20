package com.example.mypage.nearby

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
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

    // 기기 광고 시작
    fun startAdvertising() {

        val advertisingName = "${localEndpointName}"
        Log.d("NearbyDebug", "1. Advertising 시작 시도: $advertisingName")

        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startAdvertising("${localEndpointName}", SERVICE_ID, lifecycleCallback, options)
            .addOnSuccessListener {
                onEvent(NearbyManagerEvent.StatusUpdate("광고가 시작되었습니다. (탐색 가능)"))
                Log.d("NearbyDebug", "1-1. Advertising 성공 (광고 중...)")
            }
            .addOnFailureListener { e ->
                onEvent(NearbyManagerEvent.Error("광고 시작 실패: ${e.localizedMessage}"))
                Log.e("NearbyDebug", "1-2. Advertising 실패: ${e.message}")
            }
    }

    // 1:N으로 유저탐색
    fun startDiscovery() {

        Log.d("NearbyDebug", "2. Discovery 시작 시도...")

        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()
        client.startDiscovery(SERVICE_ID, discoveryCallback, options)
            .addOnSuccessListener {
                onEvent(NearbyManagerEvent.StatusUpdate("주변 기기 탐색 중..."))
                Log.d("NearbyDebug", "2-1. Discovery 성공 (탐색 중...)")
            }
            .addOnFailureListener { e ->
                onEvent(NearbyManagerEvent.Error("탐색 시작 실패: ${e.localizedMessage}"))
                Log.e("NearbyDebug", "2-2. Discovery 실패: ${e.message}")
            }
    }

    // 연결 시도
    fun requestConnection(endpointId: String) {
        Log.d("NearbyDebug", "3. RequestConnection 호출 -> target ID: $endpointId")

        client.requestConnection(localEndpointName, endpointId, lifecycleCallback)
            // 연결 과정에서 발생하는 에러 메시지 추가
            .addOnFailureListener { exception ->

                Log.e("NearbyDebug", "3-2. RequestConnection 실패: ${exception.message}")

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
            .addOnSuccessListener {
                Log.d("NearbyDebug", "3-1. RequestConnection 요청 성공 (승인 대기)")
            }
    }

    // 인증 후 연결 승인
    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
    }

    // 데이터 전송
    fun sendUserCard(endpointId: String, card: UserCard) {
        client.sendPayload(endpointId, Payload.fromBytes(card.toJson().toByteArray()))
            .addOnSuccessListener {
                onEvent(NearbyManagerEvent.StatusUpdate("카드 전송이 완료되었습니다. 연결을 종료합니다."))
                //disconnect(endpointId)
            }
            .addOnFailureListener {
                onEvent(NearbyManagerEvent.Error("카드 전송에 실패했습니다."))
            }
    }

    //특정 기기와의 연결 해제 함수
    fun disconnect(endpointId: String) {
        client.disconnectFromEndpoint(endpointId)
    }

    // 모든 통신 정리
    fun stopAll() {
        client.stopDiscovery()
        client.stopAdvertising()
    }

    fun stopDiscovery(){
        client.stopDiscovery()
    }

    fun stopAdvertising(){
        client.stopAdvertising()
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            // 구글에서 자동 생성한 6자리 인증번호 이벤트 전달
            //onEvent(NearbyManagerEvent.AuthVerification(id, info.authenticationDigits))

            Log.d("NearbyDebug", "4. ConnectionInitiated 발생 -> ID: $id, AuthCode: ${info.authenticationDigits}")
            Log.d("NearbyDebug", "4-1. acceptConnection 자동 호출!")
            //자동으로 accept 되게
            acceptConnection(id)
        }

        override fun onConnectionResult(id: String, res: ConnectionResolution) {
            Log.d("NearbyDebug", "5. ConnectionResult 수신 -> Success: ${res.status.isSuccess}, Status: ${res.status.statusCode}")
            if (res.status.isSuccess) {
                client.stopDiscovery() // 연결 성공 시 탐색 중지
                client.stopAdvertising() // 연결 성공 시 광고 중지
                onEvent(NearbyManagerEvent.ConnectionSuccess(id))
            }
            else{
                onEvent(NearbyManagerEvent.Error("연결이 거절되었거나 실패했습니다."))
            }
        }

        override fun onDisconnected(id: String) {
            Log.d("NearbyDebug", "X. Disconnected 발생: $id")
            onEvent(NearbyManagerEvent.StatusUpdate("연결이 끊어졌습니다."))
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val json = String(bytes)

            //안전 종료를 위해 ACK
            if (json == "ACK_RECEIVED") {
                Log.d("NearbyDebug", "상대방 데이터 수신 확인(ACK) -> 소켓 안전 해제")
                disconnect(id) // 전송 완료 후 연결 안전 종료
                return
            }

            Log.d("NearbyDebug", "6. Payload 수신 완료! -> Data: $json")
            try {
                val card = UserCard.fromJson(json)
                onEvent(NearbyManagerEvent.UserCardReceived(card))

                //수신 성공 시 ACK 보내고 종료
                client.sendPayload(id, Payload.fromBytes("ACK_RECEIVED".toByteArray()))
                    .addOnCompleteListener {
                        Log.d("NearbyDebug", "ACK 전송 완료 후 내 쪽 소켓 정리")
                        // 수신 측도 전송 완료 후 소켓 해제
                        disconnect(id)
                    }

            } catch (e: Exception) {
                onEvent(NearbyManagerEvent.Error("유저카드 파싱에 실패했습니다."))
            }
        }

        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
            Log.d("NearbyDebug", "6-1. Payload 전송 상태 업데이트 -> Bytes: ${update.bytesTransferred}/${update.totalBytes}")
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            Log.d("NearbyDebug", "2-3. 주변 기기 발견(EndpointFound)! ID: $id, Name: ${info.endpointName}")
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
    data class StatusUpdate(val message: String) : NearbyManagerEvent()
    data class Error(val message: String) : NearbyManagerEvent()
}