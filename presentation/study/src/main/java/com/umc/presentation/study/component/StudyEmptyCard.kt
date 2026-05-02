package com.umc.presentation.study.component

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

/**
 * 스터디/활동 목록이 비어있을 때 표시되는 카드
 */
@Composable
fun StudyEmptyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_book),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = neutral400()
            )
            Spacer(Modifier.height(12.dp))
            UText(
                text = AppStrings.STUDY_EMPTY,
                style = UmcTypographyTokens.FootnoteBold,
                color = neutral500(),
            )
        }
    }
}