package com.umc.component.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main500
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 공지 작성에서 사용하던 선택 목록. 선택 결과는 표시 순서의 인덱스로 전달한다.
 * [selectedIndex]가 null이면 선택 강조 없이 표시한다.
 * [headerContent]로 검색 입력 등을 넣을 수 있으며, 빈 목록 안내는 [emptyContent]로 전달한다.
 */
@Composable
fun USelectSheetContent(
    title: String,
    items: ImmutableList<String>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    contentPadding: PaddingValues = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
    onSelect: (Int) -> Unit = {},
    headerContent: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth().padding(contentPadding)) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey950(),
        )
        Spacer(Modifier.height(8.dp))
        headerContent()

        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            itemsIndexed(items) { index, item ->
                UText(
                    text = item,
                    style = UmcTypographyTokens.Body,
                    color = if (selectedIndex == index) main500() else grey800(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(index) }
                        .padding(vertical = 16.dp),
                )
            }
            if (items.isEmpty()) {
                item { emptyContent() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun USelectSheetContentPreview() {
    UmcTheme {
        USelectSheetContent(
            title = "지부를 선택하세요",
            items = persistentListOf("전체", "중앙지부"),
        )
    }
}
