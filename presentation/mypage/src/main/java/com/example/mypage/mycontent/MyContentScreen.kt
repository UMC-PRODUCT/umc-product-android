package com.example.mypage.mycontent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral800
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyContentRoute(
    showType: String,
    viewModel: MyContentViewModel = hiltViewModel(),
    onNavigateToPostDetail: (Long) -> Unit
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //타입에 따른 UI 분기
    LaunchedEffect(showType) {
        viewModel.initShowType(showType)
    }

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