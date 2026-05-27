package com.umc.presentation.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.presentation.study.admin.submit.AdminSubmitRoute
import kotlinx.coroutines.launch

@Composable
fun ActStudyRoute() {
    ActStudyScreen()
}

@Composable
fun ActStudyScreen() {
    val tabs = listOf("제출 현황", "스터디 그룹")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
    ) {
        // 커스텀 탭
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(neutral100(), RoundedCornerShape(1000.dp))
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (isSelected) 2.dp else 0.dp,
                            shape = RoundedCornerShape(1000.dp)
                        )
                        .background(
                            color = if (isSelected) neutral000() else neutral100(),
                            shape = RoundedCornerShape(1000.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = HeadlineBold,
                        color = if (isSelected) neutral800() else neutral400()
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AdminSubmitRoute()
                1 -> { /* TODO: 스터디 그룹 */ }
            }
        }
    }
}