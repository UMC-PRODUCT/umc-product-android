package com.umc.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.domain.model.enums.UserType
import com.umc.component.R
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral700
import com.umc.component.theme.primary100
import com.umc.component.theme.primary600

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
            //XML의 UButton 커스텀 속성들을 Surface와 Text로 구현
            Surface(
                color = if (userType == UserType.ACTIVE) primary100() else neutral200(),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = userType.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = UmcTypographyTokens.FootnoteBold,
                    color = if (userType == UserType.ACTIVE) primary600() else neutral700()
                )
            }

            // 알림 버튼 (상태에 따라 점이 있는 아이콘으로 교체)
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (alarmExist) R.drawable.ic_alarm_with_dot else R.drawable.ic_alarm
                    ),
                    contentDescription = "Notification",
                    tint = neutral500()
                )
            }
        }
    }
}