package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.presentation.study.admin.submit.AdminSubmitItemUiModel

@Composable
fun AdminSubmitItem(
    item: AdminSubmitItemUiModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(grey000(), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = grey200(), shape = CircleShape)
                .background(grey000()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 이름, 파트, 학교, 스터디 제목
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UText(
                    text = "${item.nickname}(${item.name})",
                    style = BodyBold,
                    color = grey800()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, grey200(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    UText(text = item.partLabel, style = Caption1Bold, color = grey700())
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                UText(text = item.schoolName, style = Caption1, color = grey500())
                UText(text = " | ", style = Caption1, color = grey500())
                UText(
                    text = item.studyTitle,
                    style = Caption1,
                    color = grey500(),
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
                        .background(green100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    UText(text = "Pass", style = Caption1Bold, color = green500())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            if (item.markStatus == "FAIL") {
                Box(
                    modifier = Modifier
                        .background(red100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    UText(text = "Fail", style = Caption1Bold, color = red700())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            if (item.isBest) {
                Box(
                    modifier = Modifier
                        .background(yellow100(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    UText(text = "Best", style = Caption1Bold, color = yellow500())
                }
                Spacer(modifier = Modifier.width(7.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey500(),
                modifier = Modifier.size(7.dp, 14.dp)
            )
        }
    }
}