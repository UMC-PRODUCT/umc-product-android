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

/** 통신 흐름 (QR코드 스캔 + nearbyconnection) / 그냥은 3번 이후부터
 *
 * [기기 A (QR 스캐너)]                                [기기 B (QR 노출자)]
 *         │                                                    │
 *   1. CameraX로 B의 QR 스캔                                   │
 *      ('SM-T733' 획득)                                         │
 *         │                                            startAdvertising()
 *   startDiscovery()                                   (Name: 'SM-T733')
 *         │                                                    │
 *         ├──────────────── 2. EndpointFound ──────────────────┤
 *         │                (ID: EX5J, Name: SM-T733)          │
 *         │                                                    │
 *   3. QrContent('SM-T733') == EndpointName('SM-T733') Match!  │
 *         │                                                    │
 *   requestConnection('EX5J') ──────────────┐                  │
 *         │                                 ▼                  │
 *   4. onConnectionInitiated 발생 ◄────────────────────────────┤
 *      (acceptConnection 자동 승인)                             │
 *         │                                                    │
 *   5. ConnectionResult (Success) ─────────────────────────────┤
 *         │                                                    │
 *   sendUserCard() (UserCard JSON 전송) ────────────────────────►│
 *         │                                              Payload 수신!
 *         │                                              (UserCardReceived)
 *         │                                                    │
 *         │◄───────── 6. ACK_RECEIVED 페이로드 답장 ──────────────┤
 *         │                                              disconnect('EX5J')
 *   ACK 수신 확인!                                              │
 *   disconnect('EX5J')                                         │
 *   (안전하게 소켓 정리)                                         │
 *
 *
 * **/

/**
 * Nearby Connections API를 총괄 관리하는 클래스
 * @param context 애플리케이션 컨텍스트
 * @param onEvent ViewModel로 통신 상태 및 데이터 이벤트를 전달하는 콜백
 */
class NearbyManager(
    context: Context,
    private val onEvent: (NearbyManagerEvent) -> Unit
) {
    private val client = Nearby.getConnectionsClient(context)

    //타 앱과의 신호 혼선을 막기위한 고유 ID(패키지 이름) -
    private val SERVICE_ID = "com.example.mypage.nearby"

    //endpoint 이름 = 기기 모델명 (SM-G991N)
    private val localEndpointName = Build.MODEL

    //주변 기기에 내 기기를 광고 시작
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

    //주변에서 광고중인 기기를 1:N으로 탐색
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

    /**
     * 발견된 특정 endpointId 기기에 P2P 연결 요청
     * @param endpointId 구글 Nearby API에서 발급한 상대 기기의 일회성 고유 식별자
     */
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
                //에러 메시지를 UI에 전달 (이벤트를 새롭게 추가해야 합니다)
                onEvent(NearbyManagerEvent.Error(message))
            }
            .addOnSuccessListener {
                Log.d("NearbyDebug", "3-1. RequestConnection 요청 성공 (승인 대기)")
            }
    }

    //상대방과의 연결을 승인
    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
    }

    /**
     * 상대방 기기로 내 UserCard 객체(JSON) 전송
     * @param endpointId 대상 기기의 ID
     * @param card 전송할 유저 명함 데이터
     */
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

    //모든 통신 정리
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

    /**
     * 연결 수립 과정에서의 기기 간 핸드셰이킹 콜백
     */
    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        //상대방과의 연결 파이프라인 첫 수립시
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            //구글에서 자동 생성한 6자리 인증번호 이벤트 전달
            //onEvent(NearbyManagerEvent.AuthVerification(id, info.authenticationDigits))

            Log.d("NearbyDebug", "4. ConnectionInitiated 발생 -> ID: $id, AuthCode: ${info.authenticationDigits}")
            Log.d("NearbyDebug", "4-1. acceptConnection 자동 호출!")

            //인증번호 검증 없이 자동으로 accept 되게
            acceptConnection(id)
        }

        //연결 승인 결과 (성공/실패)
        override fun onConnectionResult(id: String, res: ConnectionResolution) {
            Log.d("NearbyDebug", "5. ConnectionResult 수신 -> Success: ${res.status.isSuccess}, Status: ${res.status.statusCode}")
            if (res.status.isSuccess) {
                client.stopDiscovery() //연결 성공 시 탐색 중지
                client.stopAdvertising() //연결 성공 시 광고 중지
                onEvent(NearbyManagerEvent.ConnectionSuccess(id))
            }
            else{
                onEvent(NearbyManagerEvent.Error("연결이 거절되었거나 실패했습니다."))
            }
        }

        //연결이 종료되었을 때
        override fun onDisconnected(id: String) {
            Log.d("NearbyDebug", "X. Disconnected 발생: $id")
            onEvent(NearbyManagerEvent.StatusUpdate("연결이 끊어졌습니다."))
        }
    }

    /**
     * 데이터 페이로드 송수신 처리 콜백
     */
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val json = String(bytes)

            //경우1. 내가 보낸 데이터를 상대방이 잘 받았다는 ACK 전송시 안전 종료 과정
            if (json == "ACK_RECEIVED") {
                Log.d("NearbyDebug", "상대방 데이터 수신 확인(ACK) -> 소켓 안전 해제")
                disconnect(id) //전송 완료 후 연결 안전 종료
                return
            }

            //경우2. 상대방이 보낸 데이터를 받았을 때 ACK 보내고 연결 안전 종료 과정
            Log.d("NearbyDebug", "6. Payload 수신 완료! -> Data: $json")
            try {
                val card = UserCard.fromJson(json)
                onEvent(NearbyManagerEvent.UserCardReceived(card))

                //수신 성공 시 ACK 보내고 종료
                client.sendPayload(id, Payload.fromBytes("ACK_RECEIVED".toByteArray()))
                    .addOnCompleteListener {
                        Log.d("NearbyDebug", "ACK 전송 완료 후 내 쪽 소켓 정리")
                        //수신 측도 전송 완료 후 소켓 해제
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

    /**
     * 주변 기기 검색 콜백
     */
    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            Log.d("NearbyDebug", "2-3. 주변 기기 발견(EndpointFound)! ID: $id, Name: ${info.endpointName}")
            onEvent(NearbyManagerEvent.EndpointFound(id, info.endpointName))
        }

        override fun onEndpointLost(id: String) {}
    }
}



/**
 * NearbyManager 내부 이벤트를 ViewModel로 전달하는 봉인된 클래스
 */
sealed class NearbyManagerEvent {
    data class EndpointFound(val id: String, val name: String) : NearbyManagerEvent()
    data class AuthVerification(val id: String, val code: String) : NearbyManagerEvent()
    data class ConnectionSuccess(val id: String) : NearbyManagerEvent()
    data class UserCardReceived(val card: UserCard) : NearbyManagerEvent()
    data class StatusUpdate(val message: String) : NearbyManagerEvent()
    data class Error(val message: String) : NearbyManagerEvent()
}