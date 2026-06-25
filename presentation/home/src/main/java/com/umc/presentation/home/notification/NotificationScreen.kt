package com.umc.presentation.home.notification

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral800
import com.umc.domain.model.home.NotificationItem


/**
 * 알람 목록 화면
 * 
 * **/

@Composable
fun NotificationRoute(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //뒤로 가기 디스패처
    /**TODO. 삭제 - MainActivity에서 적용할 예정**/
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher


    NotificationScreen(
        notifications = uiState.notifications,
        onBackClick = { onBackPressedDispatcher?.onBackPressed() }
    )

}

@Composable
fun NotificationScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
            .padding(bottom = 32.dp)

    ) {

        //1. 상단 바
        NotificationTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(32.dp))

        //2. 알람 공지 리스트
        LazyColumn(
            modifier = Modifier.
                fillMaxSize()
        ) { }

    }

}

/**상단 top bar**/
@Composable
fun NotificationTopBar(onBackClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
            .padding(horizontal = 16.dp),
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
            text = AppStrings.HOME_NOTIFICATION_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            color = neutral800()
        )

    }
}
/*
/**알람 1개**/
@Composable
fun NotificationRow(item: NotificationItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background()
    ) {
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0)) // neutral200

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.content,
                    fontSize = 12.sp,
                    color = Color(0xFF757575) // neutral600
                )
            }
            Text(
                text = item.date,
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD), // neutral400
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
    }
}

 */