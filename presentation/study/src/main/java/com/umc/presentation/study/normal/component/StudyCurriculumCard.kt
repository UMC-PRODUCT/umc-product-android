package com.umc.presentation.study.normal.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.domain.model.enums.UserPart

/**
 * 일반 사용자의 스터디 커리큘럼 헤더 카드
 *
 * 파트명과 커리큘럼 제목을 표시합니다.
 */
@Composable
fun StudyCurriculumCard(
    part: UserPart,
    title: String,
    modifier: Modifier = Modifier,
) {
    Card(
        // 좌우 여백은 화면(UserStudyScreen)의 16dp 하나로만 둔다
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = grey50()),
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
                    tint = main500()
                )
                Spacer(Modifier.width(8.dp))
                UText(
                    text = AppStrings.STUDY_PART_CURRICULUM.format(part.label.uppercase()),
                    style = UmcTypographyTokens.SubheadlineBold,
                    color = main500(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // 커리큘럼 제목
            UText(
                text = title,
                style = UmcTypographyTokens.Title2Bold,
                color = grey800(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
