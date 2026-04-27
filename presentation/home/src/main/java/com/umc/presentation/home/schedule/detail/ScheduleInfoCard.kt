package com.umc.presentation.home.schedule.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500


/**일정의 일시와 장소를 작성하는 영역**/
@Composable
fun ScheduleInfoCard(
    todayDate: String,
    todayTime: String,
    place: String,
    onMapClick: () -> Unit
){
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, neutral300()),
        color = neutral000()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            //일시
            InfoRow(
                iconRes = R.drawable.ic_calendar_color,
                title = "일시",
                content1 = todayDate,
                content2 = todayTime
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            //장소
            InfoRow(
                iconRes = R.drawable.ic_calendar_color,
                title = "장소",
                content1 = place,
                isMapAction = true,
                onMapClick = onMapClick
            )

        }

    }
}

/**아이콘 + 제목 + 정보를 작성하는 영역(일시와 장소)**/
@Composable
fun InfoRow(
    iconRes: Int,
    title: String,
    content1: String,
    content2: String? = null,
    isMapAction: Boolean = false,
    onMapClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        //정보 아이콘
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = primary500(),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            UText(text = title,
                style = UmcTypographyTokens.BodyBold,
                color = neutral800()
            )

            Spacer(modifier = Modifier.height(8.dp))

            //정보 1 (2026.04.30 or 장소이름)
            UText(text = content1,
                style = UmcTypographyTokens.Subheadline,
                color = neutral600()
            )

            //정보 2 (시간 정보)
            if (content2 != null) {
                Spacer(modifier = Modifier.height(4.dp))
                UText(text = content2,
                    style = UmcTypographyTokens.Subheadline,
                    color = neutral600()
                )
            }

            ////지도 보기
            if (isMapAction) {
                Spacer(modifier = Modifier.height(4.dp))

                UText(
                    text = "지도 보기",
                    style = UmcTypographyTokens.Footnote,
                    color = primary500(),
                    modifier = Modifier.clickable { onMapClick?.invoke() }
                )
            }
        }
    }
}