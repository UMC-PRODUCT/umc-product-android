package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.indigo500

/**
 * 새로운 커뮤니티 스레드를 생성할 때 사용하는 플로팅 버튼
 *
 * 클릭 시 상위 화면에서 전달받은 스레드 생성 이동 로직을 실행합니다.
 */
@Composable
fun CommunityFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(
            width = 126.dp,
            height = 48.dp,
        ),
        shape = RoundedCornerShape(24.dp),
        color = indigo500(),
        shadowElevation = 6.dp,
    ) {
        // 새 스레드 아이콘과 텍스트 표시
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                tint = grey000(),
                modifier = Modifier.size(19.dp),
            )

            UText(
                text = "새 스레드",
                style = UmcTypographyTokens.Subheadline,
                color = grey000(),
            )
        }
    }
}