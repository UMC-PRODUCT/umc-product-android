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

enum class ExchangeStep {
    SELECT_METHOD, // 1번째 이미지: 방식 선택
    DISCOVER_USERS  // 2번째 이미지: 주변 유저 탐색
}


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

                /**2. wifi aware 눌렀을 때, nearbyConnection 수행하고 유저 탐색**/
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

    //유저 눌렀을 때 선택 다이얼로그
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


/** 옵션 카드 (Wi-Fi, QR) **/
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

/** 2번째 이미지: 감지된 멤버 item */
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