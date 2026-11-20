package com.umc.presentation.notice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey500
import com.umc.component.theme.main500

/**
 * 공지 목록 끝에 두는 다음 페이지 요청 자리.
 *
 * 화면에 들어오면 다음 페이지를 요청한다. 요청이 실패하면 이 자리가 같은 위치에 그대로 남아 다시 요청되지 않으므로,
 * 실패했을 때는 안내 문구와 다시 시도 버튼을 보여 사용자가 직접 다시 부를 수 있게 한다.
 *
 * @param isError 직전 다음 페이지 요청이 실패했는지
 * @param onLoadNextPage 다음 페이지 요청
 */
@Composable
internal fun NoticeNextPageItem(
    isError: Boolean,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isError) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UText(
                text = AppStrings.NOTICE_LIST_ERROR,
                style = UmcTypographyTokens.Subheadline,
                color = grey500(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            UText(
                text = AppStrings.COMMON_RETRY,
                style = UmcTypographyTokens.SubheadlineBold,
                color = main500(),
                modifier = Modifier
                    .clickable(role = Role.Button) { onLoadNextPage() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    } else {
        LaunchedEffect(Unit) {
            onLoadNextPage()
        }
    }
}
