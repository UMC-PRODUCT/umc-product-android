package com.umc.presentation.study.normal.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green700
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red700
import com.umc.domain.model.enums.StudyStatus
import com.umc.presentation.study.normal.NormalStudyItemUiModel

/**
 * 스터디 제출 상태를 표시하는 뱃지
 *
 * PASS, FAIL, IN_PROGRESS 상태에 따라
 * 문구와 배경색, 글자색을 다르게 표시합니다.
 */
@Composable
fun StudyStatusBadge(
    item: NormalStudyItemUiModel,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor, label) = when (item.status) {
        StudyStatus.PASS -> Triple(
            Color(0xFFE4FCEC),
            green700(),
            AppStrings.STUDY_BADGE_PASS,
        )

        StudyStatus.FAIL -> Triple(
            red100(),
            red700(),
            AppStrings.STUDY_BADGE_FAIL,
        )

        StudyStatus.IN_PROGRESS -> Triple(
            indigo100(),
            indigo500(),
            AppStrings.STUDY_BADGE_IN_PROGRESS,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor,
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.Caption1Bold,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        )
    }
}