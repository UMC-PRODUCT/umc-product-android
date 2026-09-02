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
import com.umc.domain.model.mypage.NearbyUserInfo
import com.umc.domain.model.mypage.UserCard

/** 통신 흐름표 (nearbyconnection)
 *
 *
 * [기기 A (정보 수신)]                                [기기 B (정보 송신)]
 *         │                                                    │
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
 * 구글 Nearby Connections API의 P2P 통신 파이프라인 전체를 저수준에서 제어하는 핵심 매니지먼트 클래스
 *
 * Wi-Fi Direct 및 Bluetooth LE 기반의 P2P_CLUSTER 전략을 사용하여 주변 기기를 수신/발견하고,
 * 핸드셰이킹 연결 수립, UserCard 페이로드 바이트 전송, ACK 수신 확인 후 소켓 안전 해제 과정을 총괄합니다.
 *
 *
 * @param context 애플리케이션 컨텍스트
 * @param onEvent ViewModel로 통신 상태 및 데이터 이벤트를 전달하는 콜백
 *
 * [유의사항]
 * - SERVICE_ID 매개변수는 타 앱과의 신호 혼선을 차단하는 고유 ID이므로 패키지명("com.example.mypage.nearby")을 유지해야 합니다.
 * - Strategy.P2P_CLUSTER 전략은 1:N 근거리 매칭에 최적화되어 있으나, 통신 성공 직후 스캔을 중단해 주어야 블루투스/Wi-Fi 간섭을 방지할 수 있습니다.
 */

class NearbyManager(
    context: Context,
    private val onEvent: (NearbyManagerEvent) -> Unit
) {
    // 구글 플레이 서비스 Nearby Connections 클라이언트 객체
    private val client = Nearby.getConnectionsClient(context)

    //타 앱과의 신호 혼선을 막기위한 고유 ID(패키지 이름) -
    private val SERVICE_ID = "com.example.mypage.nearby"

    //endpoint 이름 = 로컬 디바이스 모델명 (기본 디버깅용) ex.(SM-G991N)
    private val localEndpointName = Build.MODEL

    /**
     * 주변 기기들에게 내 존재를 알리기 위해 광고(Advertising) 세션을 시작하는 메서드
     *
     * @param userInfo 광고 헤더(endpointName)에 실어 보낼 내 근거리 요약 프로필
     */
    fun startAdvertising(userInfo: NearbyUserInfo) {

        val advertisingName = userInfo.toJson()
        Log.d("NearbyDebug", "1. Advertising 시작 시도: $advertisingName")

        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startAdvertising(advertisingName, SERVICE_ID, lifecycleCallback, options)
            .addOnSuccessListener {
                onEvent(NearbyManagerEvent.StatusUpdate("광고가 시작되었습니다. (탐색 가능)"))
                Log.d("NearbyDebug", "1-1. Advertising 성공 (광고 중...)")
            }
            .addOnFailureListener { e ->
                onEvent(NearbyManagerEvent.Error("광고 시작 실패: ${e.localizedMessage}"))
                Log.e("NearbyDebug", "1-2. Advertising 실패: ${e.message}")
            }
    }

    /**
     * 주변에서 광고 세션을 열어둔 타 기기들을 1:N 매칭으로 스캔하는 탐색(Discovery) 시작 메서드
     */
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
     * 광고와 탐색을 동시에 수행하여 양방향 기기 발굴이 가능하도록 만드는 메서드
     *
     * @param userInfo 광고 데이터로 송출할 내 정보
     */
    fun startAdvertisingAndDiscovery(userInfo: NearbyUserInfo) {
        startAdvertising(userInfo)
        startDiscovery()
    }

    /**
     * 스캔된 특정 상대방 endpointId 기기로 P2P 소켓 연결 요청을 보내는 메서드
     *
     * @param endpointId 구글 Nearby API에서 할당한 상대 기기의 고유 인스턴스 ID
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

    /**
     * 핸드셰이킹 요청을 수신했을 때 연결을 수락하고 데이터 수신 파이프라인(payloadCallback)을 연결하는 메서드
     *
     * @param endpointId 연결을 수락할 대상 기기 ID
     */
    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
    }

    /**
     * 수립된 P2P 채널을 통해 내 명함 객체(UserCard)를 바이트 페이로드 형태로 직렬화하여 전송하는 메서드
     *
     * @param endpointId 전송 대상 기기 ID
     * @param card 전송할 유저 명함 도메인 모델
     */
    fun sendUserCard(endpointId: String, card: UserCard) {
        client.sendPayload(endpointId, Payload.fromBytes(card.toJson().toByteArray()))
            .addOnSuccessListener {
                onEvent(NearbyManagerEvent.StatusUpdate("카드 전송이 완료되었습니다. 연결을 종료합니다."))
            }
            .addOnFailureListener {
                onEvent(NearbyManagerEvent.Error("카드 전송에 실패했습니다."))
            }
    }

    /**
     * 특정 상대방 기기와의 P2P 소켓 연결을 종료하는 메서드
     *
     * @param endpointId 해제할 대상 기기 ID
     */
    fun disconnect(endpointId: String) {
        client.disconnectFromEndpoint(endpointId)
    }

    /**
     * 진행 중인 탐색 및 광고 세션을 모두 중단하고 통신 리소스를 해제하는 메서드
     */
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
     * P2P 연결 수립 및 상태 변화를 감지하는 바인딩 콜백 객체
     */
    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        /**
         * 상대방 기기로부터 연결 요청이 도달했을 때 자동 승인 처리 파이프라인
         */
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            //구글에서 자동 생성한 6자리 인증번호 이벤트 전달 (기존에는 구글 인증번호 도입 -> 삭제)
            //onEvent(NearbyManagerEvent.AuthVerification(id, info.authenticationDigits))

            Log.d("NearbyDebug", "4. ConnectionInitiated 발생 -> ID: $id, AuthCode: ${info.authenticationDigits}")
            Log.d("NearbyDebug", "4-1. acceptConnection 자동 호출!")

            //인증번호 검증 없이 자동으로 accept 되게
            acceptConnection(id)
        }

        /**
         * 연결 결과(성공/거절) 수신 시 탐색/광고를 멈추고 통신 성공 이벤트를 발생시키는 콜백
         */
        override fun onConnectionResult(id: String, res: ConnectionResolution) {
            Log.d("NearbyDebug", "5. ConnectionResult 수신 -> Success: ${res.status.isSuccess}, Status: ${res.status.statusCode}")
            if (res.status.isSuccess) {
                // 채널 안정화 및 대원 혼선 방지를 위해 연결 직후 탐색/광고 중단
                client.stopDiscovery() // 연결 성공 시 탐색 중지
                client.stopAdvertising() // 연결 성공 시 광고 중지
                onEvent(NearbyManagerEvent.ConnectionSuccess(id))
            }
            else{
                onEvent(NearbyManagerEvent.Error("연결이 거절되었거나 실패했습니다."))
            }
        }

        /**
         * 소켓 연결이 끊어졌을 때 발생하는 콜백
         */
        override fun onDisconnected(id: String) {
            Log.d("NearbyDebug", "X. Disconnected 발생: $id")
            onEvent(NearbyManagerEvent.StatusUpdate("연결이 끊어졌습니다."))
        }
    }

    /**
     * 바이트 데이터 페이로드 송수신 및 ACK 응답 처리를 담당하는 콜백 객체
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
     * 주변 광고 디바이스를 감지했을 때 실행되는 탐색 콜백 객체
     */
    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            Log.d("NearbyDebug", "2-3. 주변 기기 발견(EndpointFound)! ID: $id, Name: ${info.endpointName}")

            //endpointName(JSON)을 파싱하여 NearbyUserInfo로 복원
            val parsedUserInfo = NearbyUserInfo.fromJson(info.endpointName)
                ?: NearbyUserInfo(name = info.endpointName, info = "기본 테스트 정보")

            //UI 이벤트 전달
            onEvent(NearbyManagerEvent.EndpointFound(id, parsedUserInfo))
        }

        override fun onEndpointLost(id: String) {}
    }
}



/**
 * NearbyManager 내부 통신 이벤트를 ViewModel 레이어로 전달하기 위한 봉인된 클래스
 */
sealed class NearbyManagerEvent {
    data class EndpointFound(val id: String, val userInfo: NearbyUserInfo) : NearbyManagerEvent()
    data class AuthVerification(val id: String, val code: String) : NearbyManagerEvent()
    data class ConnectionSuccess(val id: String) : NearbyManagerEvent()
    data class UserCardReceived(val card: UserCard) : NearbyManagerEvent()
    data class StatusUpdate(val message: String) : NearbyManagerEvent()
    data class Error(val message: String) : NearbyManagerEvent()
}