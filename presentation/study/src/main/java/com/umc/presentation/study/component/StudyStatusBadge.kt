package com.umc.presentation.study.component


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.presentation.study.ActStudyItemUiModel

/**
 * 학습 상태 뱃지
 *
 * 상태별 색상:
 * - Pass: 초록색
 * - Fail: 빨간색
 * - In Progress: 파랑ㅊㅍ
 */
@Composable
fun StudyStatusBadge(
    item: ActStudyItemUiModel,
    modifier: Modifier = Modifier,
) {
    val (bg, textColor, label) = when {
        item.status == StudyStatus.PASS ->
            Triple(success100(), success700(), AppStrings.STUDY_BADGE_PASS)
        item.status == StudyStatus.FAIL ->
            Triple(danger100(), danger700(), AppStrings.STUDY_BADGE_FAIL)
        item.submitState == SubmitState.REQUESTED ->
            Triple(primary100(), primary500(), AppStrings.STUDY_BADGE_IN_PROGRESS)
        else ->
            Triple(primary100(), primary500(), AppStrings.STUDY_BADGE_IN_PROGRESS)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
        modifier = modifier,
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.Caption1Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}