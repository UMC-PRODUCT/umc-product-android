package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreatePartUiModel

private val groupCreateParts = listOf(
    AdminStudyGroupCreatePartUiModel(1L, "PM"),
    AdminStudyGroupCreatePartUiModel(2L, "Design"),
    AdminStudyGroupCreatePartUiModel(3L, "Android"),
    AdminStudyGroupCreatePartUiModel(4L, "iOS"),
    AdminStudyGroupCreatePartUiModel(5L, "Web"),
    AdminStudyGroupCreatePartUiModel(6L, "Server")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreatePartBottomSheet(
    selectedPart: AdminStudyGroupCreatePartUiModel?,
    onDismissRequest: () -> Unit,
    onPartSelected: (AdminStudyGroupCreatePartUiModel) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = neutral000(),
        dragHandle = { BottomSheetDefaults.DragHandle(color = neutral600()) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            UText(
                text = "파트를 선택하세요",
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(groupCreateParts, key = { it.id }) { part ->
                    UText(
                        text = part.label,
                        style = UmcTypographyTokens.SubheadlineBold,
                        color = if (selectedPart?.id == part.id) primary500() else neutral800(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPartSelected(part)
                                onDismissRequest()
                            }
                            .padding(vertical = 16.dp)
                    )

                    HorizontalDivider(color = neutral200(), thickness = 0.5.dp)
                }
            }
        }
    }
}