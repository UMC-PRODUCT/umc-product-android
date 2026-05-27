package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.presentation.study.admin.submit.AdminSubmitItemUiModel

@Composable
fun AdminSubmitItem(
    item: AdminSubmitItemUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(neutral000(), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = neutral200(), shape = CircleShape)
                .background(neutral000()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = neutral400(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 이름, 파트, 학교, 스터디 제목
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.nickname}(${item.name})",
                    style = BodyBold,
                    color = neutral800()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, neutral200(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = item.partLabel, style = Caption1Bold, color = neutral700())
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.schoolName, style = Caption1, color = neutral500())
                Text(text = " | ", style = Caption1, color = neutral500())
                Text(
                    text = item.studyTitle,
                    style = Caption1,
                    color = neutral500(),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 상태 뱃지
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.markStatus == "PASS" || item.isBest) {
                Box(
                    modifier = Modifier
                        .background(success100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Pass", style = Caption1Bold, color = success700())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            if (item.markStatus == "FAIL") {
                Box(
                    modifier = Modifier
                        .background(danger100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Fail", style = Caption1Bold, color = danger700())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            if (item.isBest) {
                Box(
                    modifier = Modifier
                        .background(warning100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Best", style = Caption1Bold, color = warning500())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral500(),
                modifier = Modifier.size(7.dp, 14.dp)
            )
        }
    }
}