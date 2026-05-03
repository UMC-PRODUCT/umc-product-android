package com.umc.presentation.home.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UButton
import com.umc.component.theme.UmcTypographyTokens
import androidx.compose.ui.graphics.Color
import com.umc.component.component.UText
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.success100
import com.umc.component.theme.success500

/**
 * ACTIVE 유저 전용 상점/벌점/총합 점수판 카드
 */
@Composable
fun HomeActivityStatusCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = neutral200()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier
            .padding(16.dp)
        ) {

            //X기 활동 상태
            UText(
                text = uiState.activeString,
                style = UmcTypographyTokens.HeadlineBold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(12.dp)
            )

            //상벌점 판
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = neutral000()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //상점
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = "상점",
                        score = uiState.sangjum,
                        color = success500(),
                        bgColor = success100()
                    )

                    VerticalDivider()

                    //벌점
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = "벌점",
                        score = uiState.buljum,
                        color = danger500(),
                        bgColor = danger100()
                    )

                    VerticalDivider()

                    //총합
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = "총합",
                        score = uiState.total,
                        color = primary500(),
                        bgColor = primary100()
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    modifier: Modifier,
    label: String,
    score: Int,
    color: Color,
    bgColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        //UButton 컴포저블을 활용하여 일관된 디자인 유지
        UButton(
            text = label,
            backgroundColor = bgColor,
            textColor = color,
            textStyle = UmcTypographyTokens.Caption1Bold,
            cornerRadius = 4.dp,
            onClick = {}
        )
        UText(
            text = score.toString(),
            modifier = Modifier
                .padding(top = 8.dp),
            style = UmcTypographyTokens.HeadlineBold,
            color = neutral900()
        )
    }
}

//카드 분할 선
@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier
        .width(1.dp)
        .height(54.dp)
        .background(neutral200())
    )
}