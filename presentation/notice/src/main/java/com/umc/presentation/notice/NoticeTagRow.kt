package com.umc.presentation.notice

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 공지 상단의 태그 목록.
 */
@Composable
fun NoticeTagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        tags.forEach { tag ->
            Text(text = tag)
        }
    }
}
