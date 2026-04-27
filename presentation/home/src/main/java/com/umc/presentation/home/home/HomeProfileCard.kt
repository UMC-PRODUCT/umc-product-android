package com.umc.presentation.home.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.HuggText
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary600
import com.umc.domain.model.enums.UserType

/**
 * 프로필 카드 (OB/ACTIVE)
 * **/
@Composable
fun HomeProfileCard(uiState: HomeUiState) {

    //서로 다른 색깔을 부여하도록 텍스트 수정
    val growthText = buildAnnotatedString {
        //강조할 부분
        withStyle(style = SpanStyle(color = primary600())) {
            append("${uiState.growDay}일째")
        }
        //나머지 부분
        withStyle(style = SpanStyle(color = neutral800())) {
            append(" 성장하고 있어요")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = primary100()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                // 기수 태그 (FlexboxLayout 대응)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.gisuTag.forEach { tag ->
                        UButton(
                            text = tag,
                            cardBackgroundColor = neutral000(),
                            textColor = primary600(),
                            textAppearance = UmcTypographyTokens.Caption1Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 유저 이름 (닉네임 + 실명)
                UText(
                    text = "${uiState.userNickName}(${uiState.userName})",
                    style = UmcTypographyTokens.Title3,
                    color = neutral700()
                )

                //성장 일수 (Spannable 대신 하위 함수 호출)
                HuggText(
                    text = growthText,
                    style = UmcTypographyTokens.Title3Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Image(
                painter = painterResource(
                    id = if (uiState.userType == UserType.ACTIVE) R.drawable.ic_home_active else R.drawable.ic_home_ob
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}