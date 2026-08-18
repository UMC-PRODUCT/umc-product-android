package com.umc.presentation.study.normal.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green100
import com.umc.component.theme.green700
import com.umc.component.theme.red100
import com.umc.component.theme.red700
import com.umc.domain.model.enums.StudyStatus
import com.umc.presentation.study.normal.NormalStudyItemUiModel

/**
 * 스터디 항목을 펼쳤을 때 제출 결과에 따른 피드백을 표시합니다.
 *
 * PASS와 FAIL 상태에 따라 각각 다른 상태 배너를 표시합니다.
 */
@Composable
fun StudyExpandedContent(
    item: NormalStudyItemUiModel,
) {
    when (item.status) {
        StudyStatus.PASS -> {
            StudyStatusBanner(
                iconRes = R.drawable.ic_check_success,
                text = item.feedbackMessage
                    ?: AppStrings.STUDY_STATUS_PASS,
                textColor = green700(),
                backgroundColor = green100(),
            )
        }

        StudyStatus.FAIL -> {
            StudyStatusBanner(
                iconRes = R.drawable.ic_check_failed,
                text = item.feedbackMessage
                    ?: AppStrings.STUDY_STATUS_FAIL,
                textColor = red700(),
                backgroundColor = red100(),
            )
        }

        StudyStatus.IN_PROGRESS -> Unit
    }
}

/**
 * 스터디 제출 결과 메시지를 상태별 색상과 아이콘으로 표시하는 배너
 */
@Composable
private fun StudyStatusBanner(
    iconRes: Int,
    text: String,
    textColor: Color,
    backgroundColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = textColor,
            )

            Spacer(modifier = Modifier.width(6.dp))

            UText(
                text = text,
                style = UmcTypographyTokens.FootnoteBold,
                color = textColor,
            )
        }
    }
}