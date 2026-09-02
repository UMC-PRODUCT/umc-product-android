package com.example.mypage.mycard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.mypage.UserCard
import com.umc.component.R
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey000
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey950


/**
 * 명함 교환 트랜잭션이 성공적으로 완료되었을 때 수신된 명함 정보와 완료 안내를 노출하는 전면 오버레이 컴포저블
 *
 */
@Composable
fun CardExchangeSuccessOverlay(
    receivedCard: UserCard?,
    onContinueExchange: () -> Unit, //[계속 교환하기] -> 탐색 다이얼로그 재노출
    onConfirm: () -> Unit           //[확인] -> 오버레이 닫고 MycardScreen으로 복귀
) {
    // 수신받은 카드 객체에서 상대방 이름을 파싱
    val receivedName = receivedCard?.name?.ifEmpty { "테스트 이름" } ?: "null 이름"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_card_complete),
                contentDescription = "Exchange Success",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            //타이틀
            UText(
                text = AppStrings.EXCHANGE_CARD_SUCCESS_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950()
            )

            Spacer(modifier = Modifier.height(4.dp))

            //서브 타이틀
            UText(
                text = "${receivedName}님의 명함이 명함첩에 저장됐어요.",
                style = UmcTypographyTokens.Subheadline,
                color = grey600()
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 계속 교환하기 (주요 버튼)
            UButton(
                text = "계속 교환하기",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 12.dp,
                onClick = onContinueExchange
            )

            // 확인 (보조 버튼)
            UButton(
                text = "확인",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = grey100(),
                textColor = grey700(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 12.dp,
                onClick = onConfirm
            )
        }
    }
}