package com.example.mypage.mycontent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UCommunityItemCard
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyContentRoute(
    viewModel: MyContentViewModel = hiltViewModel(),
    onNavigateToPostDetail: (Long) -> Unit
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current


    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is MyContentEvent.ClickBackPressed -> {
                    /**TODO 차후 activity단으로 */
                }
                is MyContentEvent.ShowErrorToast -> {
                    //Toast.makeText(context, event.errorMessage, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    MyContentScreen(
        uiState = uiState,
        onBackPressed = viewModel::onClickBackPressed,
        onItemClick = onNavigateToPostDetail,
        onLoadNextPage = { viewModel.settingPost(isRefresh = false) }
    )

}

@Composable
fun MyContentScreen(
    uiState: MyContentUiState,
    onBackPressed: () -> Unit,
    onItemClick: (Long) -> Unit,
    onLoadNextPage: () -> Unit
) {
    val listState = rememberLazyListState()

    // 무한 스크롤 핸들링: 마지막 아이템 근처에 도달하면 다음 페이지 로드
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val totalItems = uiState.nowContents.size
                if (!uiState.isPageLoading && !uiState.isLastPage &&
                    lastIndex != null && lastIndex >= totalItems - 2) {
                    onLoadNextPage()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100())
            .padding(horizontal = 16.dp)
    ) {
        //상단 바
        MyContentTopBar(
            showType = uiState.showType,
            onBackClick = onBackPressed
        )

        if (!uiState.isContents && !uiState.isPageLoading) {
            //빈 화면 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = "게시글이 존재하지 않습니다.",
                    style = UmcTypographyTokens.Footnote,
                    color = neutral800()
                )
            }
        } else {
            //게시글 리스트
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.nowContents) { item ->
                    UCommunityItemCard(
                        item = item,
                        onClick = { onItemClick(item.postId) }
                    )
                }

                // 페이지 로딩 인디케이터
                if (uiState.isPageLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = primary500(),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .height(64.dp)
                    )
                }
            }
        }
    }




}

/**상단 top bar**/
@Composable
fun MyContentTopBar(
    showType: String,
    onBackClick: () -> Unit
){

    val title = when (showType) {
        "MYPOST" -> "내가 쓴 글"
        "MYCOMMENT" -> "댓글 단 글"
        "MYSCRAP" -> "스크랩"
        else -> showType
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id=R.drawable.ic_back),
            contentDescription = null,
            tint = neutral800(),
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(end = 16.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier
            .width(16.dp)
        )
        UText(
            text = title,
            style = UmcTypographyTokens.Title2Bold,
            color = neutral800()
        )

    }
}