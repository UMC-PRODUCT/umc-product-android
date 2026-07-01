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

/**
 * Best 선정 아이템에 표시되는 뱃지!
 */
@Composable
fun StudyBestBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = warning100(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_best),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = warning500(),
            )
            Spacer(Modifier.width(2.dp))
            UText(
                text = AppStrings.STUDY_BADGE_BEST,
                style = UmcTypographyTokens.Caption1Bold,
                color = warning500(),
            )
        }
    }
}