package com.example.mypage.dialog

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.mypage.NearbyUserInfo
import kotlinx.collections.immutable.ImmutableList

/**
 * 명함 교환 방식 선택 및 주변 유저 탐색을 진행하는 바텀시트 다이얼로그 컴포저블
 */
enum class ExchangeStep {
    SELECT_METHOD, // 명함 교환 방식 선택 단계
    DISCOVER_USERS  // Nearby Connections 기반 주변 유저 탐색 단계
}

/**
 * 명함 교환 시 근거리 감지된 주변 유저 목록을 노출하고 연결 다이얼로그를 호출하는 바텀시트 컴포저블
 *
 * Nearby Connections 기술로 검색된 디바이스 목록(discoveredDevices)을 수신하여 렌더링하며,
 * 특정 타겟 유저 클릭 시 명함 전송 확인 팝업(UDialog)을 띄워 연결 트랜잭션을 시작합니다.
 *
 * 주요 동작 흐름:
 * 1. 바텀시트 오픈 시 현재 수신된 주변 디바이스 데이터 유무에 따라 비어있는 안내 문구 또는 NearbyUserListItem 목록을 노출합니다.
 * 2. 특정 유저를 클릭하면 onUserSelect 콜백이 동작하여 selectedTargetUser 상태가 기입되고 전송 확인 팝업이 노출됩니다.
 * 3. 팝업에서 전송하기 클릭 시 onConfirmConnect 콜백을 통해 상대방 디바이스로 연결 및 명함 전송 패킷을 전달합니다.
 *
 * [유의사항]
 * - 기능 추가 및 교환 UX 단순화 정책으로 인해 1단계 교환 방식 선택(SELECT_METHOD: Wi-Fi Aware vs QR) 단계는 현재 사용하고 있지 않습니다.
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardExchangeBottomSheet(
    sheetState: SheetState,
    currentStep: ExchangeStep,
    discoveredDevices: ImmutableList<Pair<String, NearbyUserInfo>>, // (endpointId, userInfo) 형태로 찾은 리스트들
    selectedTargetUser: Pair<String, NearbyUserInfo>?, // 선택한 유저 정보
    onDismissRequest: () -> Unit,
    onWifiAwareClick: () -> Unit, //wifi aware 선택 시 (nearbyConnection)
    onQrCodeClick: () -> Unit, //qr코드 페이지 이동
    onUserSelect: (Pair<String, NearbyUserInfo>) -> Unit, //유저 선택 후 로직
    onConfirmConnect: () -> Unit,
    onCancelDialog: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = grey000(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            /*
            // 명함 교환 방식 선택 단계 (기능 정책 단순화로 현재 미사용 및 주석 보존 처리)
            when (currentStep) {
                /**1. 방법 선택일 경우 (선택 모습 띄우기)**/
                ExchangeStep.SELECT_METHOD -> {
                    UText(
                        text = AppStrings.EXCHANGE_CARD_TITLE,
                        style = UmcTypographyTokens.Title3Bold,
                        color = grey950()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    UText(
                        text = AppStrings.EXCHANGE_CARD_CONTENT,
                        style = UmcTypographyTokens.Subheadline,
                        color = grey600()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    //Wi-Fi Aware 고속 교환
                    ExchangeOptionCard(
                        iconRes = R.drawable.ic_wifi_send,
                        title = AppStrings.EXCHANGE_CARD_WIFI_TITLE,
                        subtitle = AppStrings.EXCHANGE_CARD_WIFI_CONTENT,
                        onClick = onWifiAwareClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR 코드
                    ExchangeOptionCard(
                        iconRes = R.drawable.ic_qrcode,
                        iconColor = grey950(),
                        title = AppStrings.EXCHANGE_CARD_QR_TITLE,
                        subtitle = AppStrings.EXCHANGE_CARD_QR_CONTENT,
                        onClick = onQrCodeClick
                    )



                    Spacer(modifier = Modifier.height(24.dp))

                    //취소하기 버튼
                    UButton(
                        text = AppStrings.EXCHANGE_CARD_CANCEL,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        backgroundColor = grey800(),
                        textColor = grey000(),
                        textStyle = UmcTypographyTokens.HeadlineBold,
                        cornerRadius = 12.dp,
                        onClick = onDismissRequest
                    )
                }

             */

            // 2. Nearby Connections 기반 주변 유저 탐색 단계 (현재 단일 진입점으로 활성화)
                //ExchangeStep.DISCOVER_USERS -> {
                    UText(
                        text = AppStrings.EXCHANGE_CARD_WIFI_USER_TITLE,
                        style = UmcTypographyTokens.Title3Bold,
                        color = grey950()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    /**유저가 없을 경우**/
                    if (discoveredDevices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UText(
                                text = AppStrings.EXCHANGE_CARD_WIFI_USER_SEARCH,
                                style = UmcTypographyTokens.Body,
                                color = grey400()
                            )
                        }
                    }
                    /**유저를 탐색한 경우**/
                    else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(300.dp)
                        ) {
                            //1번째는 string은 고유 주소 - 2번째는 nearUserInfo
                            items(discoveredDevices, key = { it.first }) { item ->
                                NearbyUserListItem(
                                    userInfo = item.second,
                                    onClick = { onUserSelect(item) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    UButton(
                        text = AppStrings.EXCHANGE_CARD_WIFI_USER_SEARCH_PAUSE,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        backgroundColor = grey800(),
                        textColor = white(),
                        textStyle = UmcTypographyTokens.HeadlineBold,
                        cornerRadius = 12.dp,
                        onClick = onDismissRequest
                    )
                //}
            }
        }
    //}

    // 목록에서 대상 유저 클릭 시 노출되는 명함 전송 확인 다이얼로그
    if (selectedTargetUser != null) {
        val targetInfo = selectedTargetUser.second
        UDialog(
            title = AppStrings.EXCHANGE_CARD_SEND_TITLE,
            content = "${targetInfo.name}님에게 명함을 전송합니다.",
            confirmText = "전송하기",
            onConfirm = onConfirmConnect,
            onDismissRequest = onCancelDialog
        )
    }
}


/**
 * 명함 교환 옵션 방식(Wi-Fi Connections, QR 코드)을 보여주는 카드 컴포저블 (현재 미사용)
 */
@Composable
private fun ExchangeOptionCard(
    iconRes: Int,
    iconColor: Color = indigo500(),
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, grey200(), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(indigo100()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            //설명 텍스트
            Column(modifier = Modifier.weight(1f)) {
                UText(
                    text = title,
                    style = UmcTypographyTokens.CalloutBold,
                    color = grey950()
                )

                Spacer(modifier = Modifier.height(2.dp))

                UText(
                    text = subtitle,
                    style = UmcTypographyTokens.Caption1,
                    color = grey600()
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Nearby Connections를 통해 근거리에서 감지된 개별 유저 항목을 표시하는 컴포저블
 */
@Composable
private fun NearbyUserListItem(
    userInfo: NearbyUserInfo,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, grey200(), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //기본 프로필 이미지
            Box(
                modifier = Modifier.size(40.dp)
            ) {
                AsyncImage(
                    model = userInfo.profileImageLink,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, grey200(), CircleShape),
                    placeholder = painterResource(R.drawable.ic_profile_default),
                    error = painterResource(R.drawable.ic_profile_default)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                UText(
                    text = userInfo.name,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950()
                )
                UText(
                    text = userInfo.info,
                    style = UmcTypographyTokens.Caption1,
                    color = grey600()
                )
            }
        }
    }
}