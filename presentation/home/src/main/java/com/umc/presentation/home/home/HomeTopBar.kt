package com.umc.presentation.home.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.domain.model.enums.UserType
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.grey700
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo600

/**
 * 로고, 알림 버튼, 유저 타입(ACTIVE/OB) 배지를 포함하는 상단 바
 */
@Composable
fun HomeTopBar(
    alarmExist: Boolean, //안 읽은 알람 표시 확인유무
    userType: UserType,  //OB / ACTIVE 유무
    onNotificationClick: () -> Unit //알람 터치 시 이동
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //UMC 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.ic_logo_umc),
            contentDescription = "UMC Logo"
        )

        //OB냐 ACTIVE냐
        Row(verticalAlignment = Alignment.CenterVertically) {
            //유저 상태 배지 (ACTIVE / OB 분기 처리)
            /*
            UButton(
                text = userType.name,
                backgroundColor = if (userType == UserType.ACTIVE) indigo100() else grey200(),
                textColor = if (userType == UserType.ACTIVE) indigo600() else grey700(),
                textStyle = UmcTypographyTokens.FootnoteBold,
                onClick = {},
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier
                .width(8.dp)
            )

             */

            // 알림 버튼 (상태에 따라 점이 있는 아이콘으로 교체)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = grey000(), shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onNotificationClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (alarmExist) R.drawable.ic_alarm_with_dot else R.drawable.ic_alarm
                    ),
                    contentDescription = "Notification",
                    tint = grey500(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}