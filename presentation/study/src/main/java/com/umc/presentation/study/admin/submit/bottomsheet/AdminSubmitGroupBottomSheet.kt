package com.umc.presentation.study.admin.submit.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import kotlinx.collections.immutable.ImmutableList
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.grey000
import com.umc.component.theme.grey800
import com.umc.presentation.study.admin.submit.AdminSubmitGroupUiModel

/**
 * 관리자 제출 현황에서 조회할 스터디 그룹을 선택하는 BottomSheet
 *
 * 전체 그룹 또는 특정 스터디 그룹을 선택할 수 있으며,
 * 선택 완료 후 BottomSheet를 닫습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubmitGroupBottomSheet(
    groups: ImmutableList<AdminSubmitGroupUiModel>,
    onSelect: (AdminSubmitGroupUiModel) -> Unit,
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
            // 그룹 선택 안내 문구
            UText(
                text = "확인할 그룹을 선택하세요",
                style = Title3Bold,
                color = grey800(),
                modifier = Modifier.padding(
                    bottom = 16.dp
                ),
            )

            // 조회 가능한 그룹 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = groups,
                    key = { group ->
                        group.id ?: -1L
                    },
                ) { group ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 선택된 그룹 전달 후 BottomSheet 닫기
                                onSelect(group)
                                onDismiss()
                            }
                            .padding(vertical = 16.dp),
                    ) {
                        UText(
                            text = group.name,
                            style = Body,
                            color = grey800(),
                        )
                    }
                }
            }
        }
    }
}