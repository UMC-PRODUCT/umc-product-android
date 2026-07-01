package com.umc.presentation.study.normal.component

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.domain.model.enums.UserPart

@Composable
fun StudyCurriculumCard(
    part: UserPart,
    title: String,
    percentText: String,
    progress: Int,
    subText: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 파트명 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_document),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = primary500()
                )
                Spacer(Modifier.width(8.dp))
                UText(
                    text = AppStrings.STUDY_PART_CURRICULUM.format(part.label.uppercase()),
                    style = UmcTypographyTokens.SubheadlineBold,
                    color = primary500(),
                    modifier = Modifier.weight(1f)
                )
                // 달성률 뱃지
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primary100(),
                ) {
                    UText(
                        text = AppStrings.STUDY_ACHIEVEMENT_BADGE,
                        style = UmcTypographyTokens.Caption1Bold,
                        color = primary500(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 제목 + 퍼센트
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                UText(
                    text = title,
                    style = UmcTypographyTokens.Title2Bold,
                    color = neutral800(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                UText(
                    text = percentText,
                    style = UmcTypographyTokens.Title1Bold,
                    color = primary500()
                )
            }

            Spacer(Modifier.height(32.dp))

            // 프로그레스바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(neutral200())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0, 100) / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(primary500())
                )
            }

            Spacer(Modifier.height(8.dp))

            UText(
                text = subText,
                style = UmcTypographyTokens.Footnote,
                color = neutral500(),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}