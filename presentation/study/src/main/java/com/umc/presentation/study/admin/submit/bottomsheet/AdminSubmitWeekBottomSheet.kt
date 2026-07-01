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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubmitWeekBottomSheet(
    weeks: List<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = neutral000(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            UText(
                text = "확인할 주차를 선택하세요",
                style = Title3Bold,
                color = neutral800(),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(weeks) { week ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(week)
                                onDismiss()
                            }
                            .padding(vertical = 16.dp)
                    ) {
                        UText(text = "${week}주차", style = Body, color = neutral800())
                    }
                }
            }
        }
    }
}