package com.umc.presentation.study.admin.submit.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Title3Bold

/**
 * 제출 현황에서 사용하는 주차 선택 UI 모델
 *
 * 화면에 표시할 주차와
 * 해당 주차의 커리큘럼 ID를 함께 관리합니다.
 */
data class AdminSubmitWeekUiModel(
    val week: Int,
    val weeklyCurriculumId: Long,
)

/**
 * 관리자 제출 현황에서 조회할 주차를 선택하는 BottomSheet
 *
 * API에서 조회 가능한 주차 목록을 표시하며,
 * 선택 완료 후 해당 주차 정보를 전달하고 BottomSheet를 닫습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubmitWeekBottomSheet(
    weeks: List<AdminSubmitWeekUiModel>,
    onSelect: (AdminSubmitWeekUiModel) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = grey000(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            // 주차 선택 안내 문구
            UText(
                text = "확인할 주차를 선택하세요",
                style = Title3Bold,
                color = grey800(),
                modifier = Modifier.padding(
                    bottom = 16.dp
                ),
            )

            // 제출 현황 조회가 가능한 주차 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(weeks) { weekItem ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 선택된 주차 전달 후 BottomSheet 닫기
                                onSelect(weekItem)
                                onDismiss()
                            }
                            .padding(vertical = 16.dp),
                    ) {
                        UText(
                            text = "${weekItem.week}주차",
                            style = Body,
                            color = grey800(),
                        )
                    }
                }
            }
        }
    }
}