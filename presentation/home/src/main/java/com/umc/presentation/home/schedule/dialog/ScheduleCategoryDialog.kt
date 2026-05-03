package com.umc.presentation.home.schedule.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.home.schedule.add.ScheduleAddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCategoryBottomSheet(
    viewModel: ScheduleAddViewModel,
    onDismissRequest: () -> Unit, //종료될때
    onConfirm: () -> Unit //확인 누를 시
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = neutral000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = neutral600())
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            //상단 타이틀 및 확인 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier
                    .weight(1f)
                ) {
                    UText(
                        text = AppStrings.HOME_PLAN_ADD_PLAN_TAG_BOTTOMSHEET_TITLE,
                        style = UmcTypographyTokens.Title3Bold,
                        color = neutral800()
                    )
                    Spacer(modifier = Modifier
                        .height(4.dp)
                    )
                    UText(
                        text = AppStrings.HOME_PLAN_ADD_PLAN_TAG_BOTTOMSHEET_CONTENT,
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600()
                    )
                }

                //확인 버튼
                UButton(
                    text = AppStrings.CONFIRM,
                    backgroundColor = primary500(),
                    textColor = neutral000(),
                    onClick = onConfirm,
                    modifier = Modifier
                        .padding(start = 8.dp)

                )
            }

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //리사이클러뷰 대응
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryItemRow(
                        item = category,
                        //토글하면 바로 ViewModel에 반영하기
                        onToggle = { viewModel.selectCategory(it) }
                    )
                }
            }
        }
    }
}



/**카테고리 1개 개별 ROW**/
@Composable
fun CategoryItemRow(
    item: CategoryItem,
    onToggle: (CategoryItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(item) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //1. 카테고리 아이콘
        Image(
            painter = painterResource(
                id = if (item.isChecked) item.selectedIcon!! else item.unselectedIcon!!
            ),
            contentDescription = null,
        )

        Spacer(modifier = Modifier
            .width(16.dp)
        )

        //2. 카테고리 이름
        UText(
            text = item.name,
            style = UmcTypographyTokens.HeadlineBold,
            color = neutral800(),
            modifier = Modifier
                .weight(1f)
        )

        //3. 체크박스
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle(item) },
            colors = CheckboxDefaults.colors(
                checkedColor = primary500(),
                uncheckedColor = neutral300(),
                checkmarkColor = neutral000()
            )
        )
    }
}