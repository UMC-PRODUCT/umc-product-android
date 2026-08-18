package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo500
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreatePartUiModel

/**
 * 스터디 그룹에서 선택 가능한 파트 목록
 */
private val groupCreateParts = listOf(
    AdminStudyGroupCreatePartUiModel(
        label = "Plan",
        value = "PLAN",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Design",
        value = "DESIGN",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Web",
        value = "WEB",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Android",
        value = "ANDROID",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "iOS",
        value = "IOS",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Node.js",
        value = "NODEJS",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Spring Boot",
        value = "SPRINGBOOT",
    ),
    AdminStudyGroupCreatePartUiModel(
        label = "Admin",
        value = "ADMIN",
    ),
)

/**
 * 스터디 그룹 생성 시 담당 파트를 선택하는 BottomSheet
 *
 * 선택 가능한 파트 목록을 표시하며,
 * 현재 선택된 파트는 강조하여 표시합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreatePartBottomSheet(
    selectedPart: AdminStudyGroupCreatePartUiModel?,
    onDismissRequest: () -> Unit,
    onPartSelected: (AdminStudyGroupCreatePartUiModel) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600(),
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            // 파트 선택 안내 문구
            UText(
                text = "파트를 선택하세요",
                style = UmcTypographyTokens.Title3Bold,
                color = grey800(),
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(
                modifier = Modifier.height(20.dp),
            )

            // 선택 가능한 전체 파트 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = groupCreateParts,
                    key = { part ->
                        part.value
                    },
                ) { part ->
                    UText(
                        text = part.label,
                        style = UmcTypographyTokens.Body,

                        // 현재 선택된 파트 강조
                        color = if (
                            selectedPart?.value == part.value
                        ) {
                            indigo500()
                        } else {
                            grey800()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPartSelected(part)
                            }
                            .padding(vertical = 16.dp),
                    )
                }
            }
        }
    }
}